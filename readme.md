# 启动项目启动
特点:
1.轻量级
2.无植入性
3.支持多种微服务的调用
4.执行器可扩展

开发环境默认接入内存数据库 H2，启动时无需本地 MySQL；如需连接正式 MySQL，可在运行参数中追加 `--spring.profiles.active=mysql` 或设置环境变量 `SPRING_PROFILES_ACTIVE=mysql`。
