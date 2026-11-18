package com.merchant.ops.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    /**
     * 模型回复内容（结构化阶段存放原始 JSON，后续阶段存放自然语言回答）
     */
    private String answer;

    /**
     * 意图标识，如 QUERY_TURNOVER、CHAT
     */
    private String intent;

    /**
     * 是否需要调用业务工具
     */
    private Boolean needTool;

    /**
     * 需要调用的工具名称，无需工具时为 null
     */
    private String toolName;
}
