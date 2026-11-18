package com.merchant.ops.ai.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 工具注册中心
 * <p>
 * 自动收集容器中所有 {@link AiTool} 实现，支持按名称查找与生成 Tool Definition 清单。
 */
@Component
public class ToolRegistry {

    private final Map<String, AiTool> toolMap;

    @Autowired
    public ToolRegistry(List<AiTool> tools) {
        this.toolMap = tools.stream()
                .collect(Collectors.toMap(AiTool::getName, Function.identity()));
    }

    /**
     * 按名称获取工具
     */
    public AiTool get(String name) {
        return toolMap.get(name);
    }

    /**
     * 是否包含指定名称的工具
     */
    public boolean has(String name) {
        return toolMap.containsKey(name);
    }

    /**
     * 所有已注册工具
     */
    public Map<String, AiTool> all() {
        return toolMap;
    }

    /**
     * 构建 LLM 可识别的 Tool Definition 数组（OpenAI function calling 格式）
     */
    public JSONArray buildToolDefinitions() {
        JSONArray tools = new JSONArray();
        for (AiTool tool : toolMap.values()) {
            JSONObject definition = new JSONObject();
            definition.put("type", "function");

            JSONObject function = new JSONObject();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            function.put("parameters", tool.getParameters() == null ? new JSONObject() : tool.getParameters());
            definition.put("function", function);

            tools.add(definition);
        }
        return tools;
    }
}