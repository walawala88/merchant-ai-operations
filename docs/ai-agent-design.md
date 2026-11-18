# AI Agent Design

## 目标

让商家使用自然语言查询真实经营数据，并在明确确认后执行菜品起售/停售。LLM 只负责理解与编排，业务数据读取和写入始终由 Java Service 执行。

## 组件

```text
AiChatController
  -> AiChatServiceImpl
     -> LlmClient
     -> ToolRegistry
        -> AiTool implementations
     -> PendingConfirmationStore
     -> SseEmitter
```

- `LlmClient`：调用 OpenAI 兼容 `chat/completions` 接口。
- `ToolRegistry`：启动时收集全部 `AiTool`，构建 Function Calling 定义。
- `AiChatServiceImpl`：维护会话、执行多轮 tool call、推送 SSE 事件。
- `PendingConfirmationStore`：保存修改型操作的一次性 UUID 令牌。

## 工具清单

| Tool | 读取/写入 | 复用服务 |
| --- | --- | --- |
| `getBusinessData` | 读 | `WorkspaceService#getBusinessData` |
| `getOrderStatistics` | 读 | `ReportService#getOrdersStatistics` |
| `getTopSellingDishes` | 读 | `ReportService#getSalesTop10` |
| `getTodayTurnover` | 读 | `WorkspaceService#getBusinessData` |
| `getDishInfo` | 读 | `DishService#listWithFlavor` |
| `startDish` | 写 | `DishService#startOrStop` |
| `stopDish` | 写 | `DishService#startOrStop` |

## Function Calling 循环

1. 拼接 system prompt、历史消息与工具定义。
2. 调用 LLM。
3. 如果返回 `tool_calls`，逐个执行工具。
4. 将 assistant 的 `tool_calls` 原样加入历史。
5. 将每个工具结果以 `role=tool` 和对应 `tool_call_id` 加入历史。
6. 再次调用 LLM。
7. 最多执行 5 轮，最终回答通过 SSE 推送。

## 写操作二次确认

1. LLM 首次调用 `startDish` 或 `stopDish`，参数中没有 `token`。
2. 工具只执行菜品查询与状态校验，不修改数据库。
3. 工具生成 `CONFIRM_REQUIRED` 响应和一次性 UUID 令牌。
4. 用户明确回复确认后，编排层允许带 token 的写工具调用。
5. 工具用 `PendingConfirmationStore.validateAndConsume` 校验操作描述并消费令牌。
6. 校验成功后调用现有 Service 修改数据。

该机制保证：

- LLM 无法自行生成有效 token。
- token 与具体菜品 ID 绑定，避免确认 A 菜品却修改 B 菜品。
- token 只能使用一次。
- 未确认请求不会产生数据修改。

## SSE 事件

| 事件 | 含义 |
| --- | --- |
| `start` | 会话开始，返回 sessionId |
| `thinking` | 当前工具编排轮次 |
| `tool` | 某个工具开始执行 |
| `tool_result` | 工具执行完成 |
| `message` | 最终回答的增量文本 |
| `done` | 本次回答完成 |
| `error` | 编排或模型调用失败 |

## 失败处理

- LLM 未配置 API Key：直接返回明确错误。
- 工具不存在或执行异常：返回结构化错误，不中断后续编排。
- SSE 推送失败：结束当前 emitter。
- 会话超过 40 条：保留 system prompt 与最近消息。
- 写操作 token 无效或操作描述不匹配：拒绝执行。

## 已知边界

- 会话历史当前保存在进程内 Map，不支持多实例共享与重启恢复。
- 修改型操作尚未加入统一审计日志与幂等键。
- 工具权限与登录员工角色尚未做细粒度绑定。
- LLM 输出内容仍需生产级内容安全与合规审查。