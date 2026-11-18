# Changelog

本项目遵循 Keep a Changelog 的基本格式。

## [1.0.0] - 2026-11-18

### Added

- 统一项目命名为 Merchant AI Operations Platform，Java 包迁移至 `com.merchant.ops`。
- 增加 AI Function Calling、7 个业务工具、最多 5 轮工具编排和 SSE 流式输出。
- 增加修改型操作的一次性确认令牌与后端校验。
- 增加 RabbitMQ 缓存失效、重试、死信队列和本地降级。
- 增加 JWT 双通道鉴权、WebSocket 订单通知与 Excel 运营报表。
- 增加 Docker Compose、GitHub Actions、单元测试与开源仓库模板。

### Changed

- Maven 模块重命名为 `merchant-common`、`merchant-pojo`、`merchant-server`。
- 数据库统一为 `merchant_ops`。
- 应用配置改为环境变量注入，移除真实数据库、Redis、OSS、微信与 LLM 凭据。
- 清理 Token 日志、调试输出与不安全的异常处理。
- 默认开发管理员密码改为 `Merchant@123`，并提示部署前修改。