# Tiny Job 项目简介

Tiny Job 是一个面向中小规模调度需求的轻量级任务平台，具备无侵入、可扩展、易部署的特性。

## 核心特性
1. **轻量级**：仅依赖 Spring Boot 与 MySQL，部署简单。
2. **无侵入性**：通过配置化执行器接入现有服务，无需改造业务代码。
3. **多种调用方式**：支持 HTTP、脚本等多种任务类型，可继续扩展。
4. **执行器可扩展**：实现 `TinyJobExecutorBaseAdapter` 接口即可扩展新的执行模式。

## 快速开始
- 后端模块：`tiny-job-admin`
- 前端模块：`tiny-job-admin-web`
- 详细 API：参见根目录 `api.md`

请先根据 `docs/user-guide.md` 完成环境准备、数据库配置与服务启动，然后通过浏览器访问管理控制台。

## 项目文档
- [调度平台设计说明](docs/design.md)
- [使用手册](docs/user-guide.md)

欢迎根据业务需求扩展执行器、完善任务生命周期管理功能。
