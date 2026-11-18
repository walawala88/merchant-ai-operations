package com.merchant.ops.ai.tool;

import com.alibaba.fastjson.JSONObject;

/**
 * AI 业务工具接口（Function Calling 的可执行单元）
 * <p>
 * 每个工具对应一个可被 LLM 调用的业务函数。
 */
public interface AiTool {

    /**
     * 工具名称（LLM 通过该名称发起调用）
     */
    String getName();

    /**
     * 工具功能描述（用于 Tool Definition，供 LLM 判断何时调用）
     */
    String getDescription();

    /**
     * 工具参数定义（JSON Schema 格式），无参数时返回空定义
     */
    JSONObject getParameters();

    /**
     * 执行工具，返回 JSON 字符串格式的结果
     *
     * @param arguments LLM 传入的参数（JSON 字符串），无参数时为 "{}"
     */
    String execute(String arguments);

    /**
     * 是否为修改型工具（会修改数据库）。
     * <p>
     * 修改型工具由编排层强制二次确认，禁止直接执行。
     */
    default boolean isWriteOperation() {
        return false;
    }
}