package com.merchant.ops.ai.llm;

import java.util.List;

/**
 * 大模型对话消息
 */
public class LlmMessage {

    /**
     * 消息角色：system / user / assistant / tool
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 仅 tool 角色需要：本条 tool 结果对应的调用 id
     */
    private String toolCallId;

    /**
     * 仅 assistant 角色需要：模型发起的工具调用列表（多轮回传用）
     */
    private List<LlmToolCall> toolCalls;

    public LlmMessage() {
    }

    public LlmMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public List<LlmToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<LlmToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
}