package com.merchant.ops.unit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.ai.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {

    @Test
    void shouldRegisterToolAndBuildOpenAiDefinition() {
        AiTool tool = new TestTool();
        ToolRegistry registry = new ToolRegistry(List.of(tool));

        assertThat(registry.has("getBusinessData")).isTrue();
        assertThat(registry.get("getBusinessData")).isSameAs(tool);

        JSONArray definitions = registry.buildToolDefinitions();
        assertThat(definitions).hasSize(1);
        JSONObject function = definitions.getJSONObject(0).getJSONObject("function");
        assertThat(function.getString("name")).isEqualTo("getBusinessData");
        assertThat(function.getJSONObject("parameters").getString("type")).isEqualTo("object");
    }

    private static final class TestTool implements AiTool {
        @Override
        public String getName() {
            return "getBusinessData";
        }

        @Override
        public String getDescription() {
            return "query business data";
        }

        @Override
        public JSONObject getParameters() {
            return new JSONObject().fluentPut("type", "object");
        }

        @Override
        public String execute(String arguments) {
            return "{}";
        }
    }
}