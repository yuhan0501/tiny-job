# Tiny Job 调度平台设计说明

## 1. 背景与目标
Tiny Job 是一个针对中小规模任务运行场景的轻量级调度平台，核心目标是以最小的部署和依赖成本提供定时任务注册、调度、触发与扩展执行能力。系统重点关注以下目标：

- **轻量化部署**：仅依赖 Spring Boot 应用与 MySQL 数据库即可运行，便于在现有基础设施快速落地。
- **安全的多线程调度**：调度线程需要在高并发下保证同一任务只会被一个调度器实例锁定并触发。
- **可扩展的执行模型**：通过执行器适配接口扩展不同的调用方式（HTTP、脚本等）。
- **可观测性**：提供后台管理界面用于查看任务状态、触发时间与筛选条件。

非目标包括：分布式一致性协调（依赖部署多实例时需额外配置锁服务）、复杂工作流编排以及自动化运维功能。

## 2. 总体架构概览
系统由三大模块组成：

1. **tiny-job-admin（后端服务）**：Spring Boot 应用，负责任务数据管理、调度线程、触发线程池以及 REST API 暴露。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L23-L61】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L28-L126】
2. **tiny-job-admin-web（前端管理端）**：React + Ant Design 管理界面，通过配置化 schema 渲染任务列表和筛选表单，并调用后端 API。【F:tiny-job-admin-web/src/schema/jobinfo.dataSchema.js†L1-L77】【F:tiny-job-admin-web/src/schema/jobinfo.querySchema.js†L1-L28】
3. **MySQL 数据库**：保存 `job_info` 与 `job_config` 等表，实现任务元数据持久化以及乐观锁控制。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/entity/JobInfo.java†L15-L104】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/entity/JobConfig.java†L13-L57】

部署上，后端服务暴露 HTTP 接口，前端静态资源可由同一服务托管或通过 webpack 构建后复制到 `tiny-job-admin` 的静态目录，数据库通过 JDBC 连接配置在 `application.yml` 中。【F:tiny-job-admin/src/main/resources/application.yml†L1-L28】

## 3. 核心数据模型
### 3.1 JobInfo
`JobInfo` 表记录了调度任务的基础元数据，包括 Cron 表达式、任务描述、阻塞策略、状态以及触发时间戳。`triggerLastTime`/`triggerNextTime` 字段用于调度窗口计算，`updateTime` 作为乐观锁字段确保并发更新安全。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/entity/JobInfo.java†L17-L87】

### 3.2 JobConfig
`JobConfig` 存储执行层面的配置，如调用服务、方法、参数以及服务类型（HTTP、脚本等）。任务加载时会通过 `configId` 关联 `JobInfo` 与 `JobConfig`，供执行器使用。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/entity/JobConfig.java†L17-L56】

## 4. 调度线程与时间窗口
`JobScheduleHelper` 在服务启动时创建守护线程，以 1 秒节拍循环扫描任务：

1. 根据当前时间 `nowTime` 与 5 秒预读窗口（`PRE_READ_TIME`）计算批量查询区间，避免逐条计算带来的 I/O 压力。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L38-L69】
2. 对每个命中的 `JobInfo` 调用 `scheduleSingleJob`，先校验 `triggerNextTime`，再尝试通过 `lockJobForSchedule` 将 `trigger_next_time` 推进到锁定时间（`now + 3 * PRE_READ_TIME`）以实现软锁。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L71-L109】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/JobInfoHelper.java†L52-L86】
3. 在持有锁的快照上，根据 Cron 规则构建调度计划，产出锁定窗口内所有触发槽位以及窗口外的下一次触发时间。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L111-L150】
4. 将触发槽位复制为不可变快照投递给触发线程池，并调用 `finalizeSchedule` 将数据库中的 `trigger_last_time`、`trigger_next_time` 更新为新的时间点。若提交失败则通过 `restoreScheduleLock` 还原原始值，确保下一轮扫描仍可拾取任务。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L118-L147】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/JobInfoHelper.java†L88-L123】

这种基于乐观锁的软锁设计保证了在多调度线程甚至多实例环境下同一任务只会被一个持锁线程推进，避免重复触发。

## 5. 触发线程池与执行器
`JobTriggerPoolHelper` 使用 `ScheduledThreadPoolExecutor` 按照调度线程计算出的延迟值触发任务：

- 线程池大小可通过 `tiny-job.triggerPollSize` 配置，默认使用 CPU 核心数，线程设置为守护线程以便应用关闭时自动退出。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L33-L62】
- 任务触发时复制 `JobInfo` 快照并绑定执行器适配器，延迟值小于等于 0 的任务直接执行，其余任务通过 `schedule` 延迟提交。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L64-L90】
- 触发线程运行 `JobTriggerThread`，最终调用注入的 `TinyJobExecutorBaseAdapter#processJob` 执行业务逻辑，实现执行层的可插拔扩展。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerThread.java†L14-L32】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/executor/TinyJobExecutorBaseAdapter.java†L11-L13】
- 服务销毁时通过 `@PreDestroy` 优雅关闭线程池，等待任务完成并在超时后强制终止，确保资源释放与任务完整性。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L92-L113】

执行器实例以 Spring Bean 形式注册到 `Map<String, TinyJobExecutorBaseAdapter>`，内部使用 `ConcurrentHashMap` 存储，保证多线程读取执行器的可见性与安全性。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L37-L54】

## 6. API 层与任务管理
`JobInfoController` 暴露任务查询与新增接口：

- `/jobinfo/list` 提供分页查询，支持状态、描述、类型筛选，通过 PageHelper 统计分页信息。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L33-L57】
- `/jobinfo/add` 接收任务数据并返回处理结果（当前实现仅记录日志，未来可接入实际持久化逻辑）。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L59-L67】

前端通过 `jobinfo.querySchema` 和 `jobinfo.dataSchema` 定义筛选表单与表格列，渲染任务列表并展现触发时间、状态等核心字段。【F:tiny-job-admin-web/src/schema/jobinfo.querySchema.js†L1-L28】【F:tiny-job-admin-web/src/schema/jobinfo.dataSchema.js†L1-L77】

## 7. 配置与运维考量
- 数据源、连接池以及服务端口配置集中在 `application.yml`，包含 JDBC URL、凭据与 HikariCP 参数，可根据环境调整。【F:tiny-job-admin/src/main/resources/application.yml†L1-L24】
- `TinyJobConfig` 负责读取自定义前缀 `tiny-job` 的配置，并暴露 `RestTemplate` Bean 供执行器使用远程调用。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/config/TinyJobConfig.java†L13-L33】
- 前端 `config.js` 可配置 API 基础路径，将请求代理至后端服务。构建脚本提供 `npm run copy` 将构建产物复制到后端静态目录统一部署。【F:tiny-job-admin-web/package.json†L1-L49】【F:tiny-job-admin-web/package.json†L60-L68】

## 8. 可靠性与扩展性
- 乐观锁 + 软锁机制确保调度幂等性，即使调度线程宕机也可依靠锁窗口超时重新拾取任务。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L95-L147】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/JobInfoHelper.java†L52-L123】
- 触发线程池的延迟调度与守护线程配置保证任务按期执行并随应用生命周期安全退出。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L47-L113】
- 执行器接口与 `JobConfig` 表设计支持自定义服务类型，可按需扩展 HTTP、RPC、脚本等多种执行方式。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/entity/JobConfig.java†L17-L56】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/executor/TinyJobExecutorBaseAdapter.java†L11-L13】

## 9. 后续演进方向
- 引入分布式锁或数据库层的乐观锁版本字段以提升多实例部署的可控性。
- 丰富 API（新增/修改/删除）并补齐前端交互，形成完整的任务生命周期管理。
- 接入监控与告警，跟踪调度线程运行状态与任务执行结果。
- 引入执行日志与失败重试策略，增强平台可维护性。
