# Contributing

感谢关注 Merchant AI Operations Platform。提交代码前请先创建 Issue 描述问题或需求。

## 开发流程

1. 从 `main` 创建功能分支，例如 `feat/tool-audit`、`fix/sse-timeout`。
2. 保持提交范围单一，建议使用 Conventional Commits。
3. 提交前执行：

```bash
mvn clean verify
```

4. Pull Request 中说明变更、验证方式和风险。

## 代码要求

- Java 17，4 空格缩进，统一 UTF-8。
- 不提交真实数据库密码、JWT 密钥、LLM API Key、OSS/微信凭据。
- 新增 AI 工具必须提供名称、描述、JSON Schema 和执行结果测试。
- 修改型 AI 工具必须接入二次确认机制。
- 修改接口、配置或数据库时同步更新 README 与示例配置。
- 外部服务集成测试应显式标记，不得阻塞默认 CI。

## Pull Request 检查

- [ ] `mvn clean verify` 通过
- [ ] 未提交 `.env`、证书、私钥或真实密钥
- [ ] 新增功能包含测试或说明无法测试原因
- [ ] 已评估缓存、消息队列和 AI 编排影响