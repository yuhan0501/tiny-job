# Tiny Job 使用手册

本手册面向运维与开发人员，指导如何部署、配置并使用 Tiny Job 调度平台的后端服务与前端管理界面。

## 1. 环境准备
- **JDK 8+ 与 Maven 3.5+**：用于编译并运行 `tiny-job-admin` Spring Boot 服务。
- **MySQL 5.7+**：用于存储任务元数据，表结构包含 `job_info`、`job_config` 等。
- **Node.js 6.x 与 npm 3.x**：用于构建或本地调试前端（版本范围与兼容性见 `package.json` 中的 engines 字段）。【F:tiny-job-admin-web/package.json†L1-L34】

> 提示：仓库未提供数据库初始化脚本，可根据实体字段创建相应表结构，字段名称与类型可参考设计文档中的数据模型章节。

## 2. 后端服务部署
1. **配置数据库**：复制 `tiny-job-admin/src/main/resources/application.yml`，根据实际环境修改 JDBC URL、用户名和密码，同时根据连接池需求调整 HikariCP 参数。【F:tiny-job-admin/src/main/resources/application.yml†L1-L24】
2. **设置线程池参数**：如需定制触发线程池大小，可在相同配置文件下调整 `tiny-job.triggerPollSize`，否则默认使用 CPU 核心数。【F:tiny-job-admin/src/main/resources/application.yml†L25-L28】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L45-L62】
3. **启动服务**：
   - 开发模式可在 `tiny-job-admin` 目录执行 `mvn spring-boot:run`；
   - 或先执行 `mvn -pl tiny-job-admin -am package` 生成可执行 JAR，然后通过 `java -jar target/tiny-job-admin-*.jar` 运行。
4. **验证运行**：服务默认监听 `8080` 端口并挂载在 `/tiny-job` 上，可访问 `http://<host>:8080/tiny-job/jobinfo/list` 验证接口。【F:tiny-job-admin/src/main/resources/application.yml†L1-L7】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L33-L57】

> 注意：首次启动若下载依赖失败，可在 Maven 配置中添加镜像源或预先在本地仓库缓存相关依赖。

## 3. 前端管理界面
1. **配置 API 地址**：根据部署环境调整 `tiny-job-admin-web/src/config.js` 中的 `api` 域名与路径，使其指向后端服务。
2. **安装依赖并启动开发服务器**：在 `tiny-job-admin-web` 目录执行 `npm install` 与 `npm run start`，默认会通过 webpack-dev-server 提供热更新环境。
3. **生产构建与集成**：执行 `npm run prod` 生成压缩版本，再运行 `npm run copy` 将 `dist` 目录中的静态资源复制到 `tiny-job-admin` 的 `src/main/resources/static` 下，后端即可直接提供前端页面。【F:tiny-job-admin-web/package.json†L35-L68】
4. **访问界面**：部署完成后，可通过 `http://<host>:8080/tiny-job/` 打开控制台。

前端界面基于配置化 schema 自动渲染查询表单与任务列表，支持按照状态（已停止/运行中）、类型（HTTP、脚本）以及关键词过滤任务，同时展示 Cron、阻塞策略、触发时间等字段。【F:tiny-job-admin-web/src/schema/jobinfo.querySchema.js†L1-L28】【F:tiny-job-admin-web/src/schema/jobinfo.dataSchema.js†L1-L77】

## 4. 任务管理流程
1. **创建任务**：
   - 在前端“新增任务”表单中填写 Cron 表达式、描述、任务类型与执行配置；
   - 或调用后端 `/jobinfo/add` 接口提交 JSON 任务定义（参考 API 章节）。当前实现会记录任务请求，后续可扩展为写入数据库。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L59-L67】
2. **查询任务**：
   - 前端列表支持分页、排序与字段筛选，内部调用 `/jobinfo/list` 接口获取 `JobInfo` 数据及分页信息。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/controller/JobInfoController.java†L33-L57】
   - 请求参数与响应字段详见 API 文档 `api.md`，包括 `currentPage`、`pageSize`、`jobStatus`、`jobDesc`、`jobType` 等。【F:api.md†L1-L52】
3. **查看调度状态**：列表中“上次触发时间”“下次触发时间”字段可帮助判断任务执行进度；字段值来源于后端调度线程对 `triggerLastTime` 与 `triggerNextTime` 的维护。【F:tiny-job-admin-web/src/schema/jobinfo.dataSchema.js†L45-L69】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L118-L147】
4. **执行与扩展**：任务触发后由 `JobTriggerPoolHelper` 调度至注册的执行器，实现 HTTP 调用或脚本执行等行为。新增执行类型时实现 `TinyJobExecutorBaseAdapter` 接口并注册为 Spring Bean 即可。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L64-L113】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/executor/TinyJobExecutorBaseAdapter.java†L11-L13】
5. **暂停/恢复任务**：任务需要临时停用时，可在列表“操作”列点击“暂停”，后端会更新 `job_status` 并阻止触发线程继续提交执行；待业务恢复后点击“恢复”，系统将按照 Cron 重新计算下一次触发时间并继续调度。【F:tiny-job-admin-web/src/schema/jobinfo.dataSchema.js†L70-L96】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/dao/JobInfoHelper.java†L150-L204】【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobTriggerPoolHelper.java†L72-L90】

## 5. API 快速参考
完整接口示例参见仓库根目录的 `api.md`：
- **查询任务** `GET /tiny-job/jobinfo/list`，返回分页列表与任务详情字段。【F:api.md†L5-L39】
- **新增任务** `POST /tiny-job/jobinfo/add`，请求体包含 `JobInfo` 与 `JobConfig` 字段。【F:api.md†L41-L75】
- **修改任务** `POST /tiny-job/jobinfo/update`，支持更新 Cron、状态等属性。【F:api.md†L77-L110】
- **删除任务** `GET /tiny-job/jobinfo/update`（接口占位，建议后续调整为 `delete` 路径）。【F:api.md†L112-L118】

## 6. 运维建议
- 调度线程使用守护线程实现自动退出，同时采用乐观锁窗口避免重复触发；建议定期监控数据库中的 `trigger_next_time` 以确认调度是否按期推进。【F:tiny-job-admin/src/main/java/com/tiny_job/admin/thread/JobScheduleHelper.java†L40-L147】
- 若需要多实例部署，需确保数据库时钟与应用服务器时钟同步，以免窗口计算出现偏差。
- 可结合应用日志与 `logback.xml` 配置（位于 `tiny-job-admin/src/main/resources/logback.xml`）调整日志级别，便于排查调度与执行问题。

通过以上步骤，运维与开发人员可以完整地部署 Tiny Job 平台，管理定时任务并根据业务需要扩展执行能力。
