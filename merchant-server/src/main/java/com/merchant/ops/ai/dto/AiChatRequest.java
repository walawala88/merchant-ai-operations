package com.merchant.ops.ai.dto;

import lombok.Data;

/**
 * AI 对话请求参数
 */
@Data
public class AiChatRequest {

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 会话 ID（可选）。用于跨轮次维护对话历史，
     * 例如"确认停售"需要携带上一轮生成的确认令牌。
     */
    private String sessionId;

    /**
     * 租户 ID，用于知识库隔离。
     */
    private String tenantId;

    /**
     * 门店 ID，用于门店级 SOP/政策过滤；平台级政策可为空。
     */
    private String storeId;
}
