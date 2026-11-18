package com.merchant.ops.ai.llm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型一次会话响应结果
 * <p>
 * 若模型请求调用工具，则 {@link LlmChatResult#toolCalls} 非空且 content 为 null/空；
 * 否则 content 为模型最终回答。
 */
@Data
public class LlmChatResult {

    /**
     * 模型回复正文（无工具调用时才有）
     */
    private String content;

    /**
     * 模型请求的工具调用列表（无工具调用时为空）
     */
    private List<LlmToolCall> toolCalls = new ArrayList<>();

    /**
     * 是否有未处理的工具调用
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}