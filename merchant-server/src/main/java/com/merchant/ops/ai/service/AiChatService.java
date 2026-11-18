package com.merchant.ops.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话服务。
 *
 * 旧的非流式 chat 方法已停用，统一通过 SSE 向前端增量推送。
 */
public interface AiChatService {

    /**
     * 发送消息并以 SSE 流式返回模型输出。
     *
     * @param message   用户消息
     * @param sessionId 会话 ID，可为空
     * @return SSE 连接对象
     */
    SseEmitter chatStream(String message, String sessionId);

    /*
     * 旧的非流式接口已停用：
     * AiChatResponse chat(String message, String sessionId);
     */
}
