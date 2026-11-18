package com.merchant.ops.ai.llm;

import lombok.Data;

/**
 * 大模型工具调用描述
 */
@Data
public class LlmToolCall {

    /**
     * 工具调用唯一标识，用于回传 tool 消息时关联
     */
    private String id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具参数（JSON 字符串）
     */
    private String arguments;
}