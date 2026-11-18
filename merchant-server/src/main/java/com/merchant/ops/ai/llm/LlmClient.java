package com.merchant.ops.ai.llm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.config.AiProperties;
import com.merchant.ops.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 大模型 HTTP 客户端（OpenAI 兼容 chat/completions 协议）
 */
@Component
@Slf4j
public class LlmClient {

    @Autowired
    private AiProperties aiProperties;

    /**
     * 发送对话请求，返回模型回复内容
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return 模型回复内容
     */
    public String chat(String systemPrompt, String userMessage) {
        String apiKey = aiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BaseException("未配置大模型 API Key");
        }

        String url = aiProperties.getBaseUrl() + "/chat/completions";
        String body = buildRequestBody(systemPrompt, userMessage);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            // 显式指定 application/json + UTF-8，避免默认 text/plain 导致中文乱码
            httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            httpPost.setConfig(buildRequestConfig());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) {
                    log.error("大模型接口调用失败, status={}, body={}", status, result);
                    throw new BaseException("AI 服务调用失败，请稍后重试");
                }
                return parseAnswer(result);
            }
        } catch (BaseException e) {
            throw e;
        } catch (IOException e) {
            log.error("大模型接口调用异常", e);
            throw new BaseException("AI 服务暂不可用，请稍后重试");
        }
    }

    /**
     * 发送支持 Function Calling 的对话请求（支持多轮 tool 消息）
     *
     * @param messages  完整消息列表（system/user/assistant/tool）
     * @param tools     工具定义数组，可为空
     * @return 模型响应，可能包含工具调用请求
     */
    public LlmChatResult chatWithTools(List<LlmMessage> messages, JSONArray tools) {
        String apiKey = aiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BaseException("未配置大模型 API Key");
        }

        String url = aiProperties.getBaseUrl() + "/chat/completions";
        String body = buildRequestBodyWithTools(messages, tools);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            httpPost.setConfig(buildRequestConfig());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) {
                    log.error("大模型接口调用失败, status={}, body={}", status, result);
                    throw new BaseException("AI 服务调用失败，请稍后重试");
                }
                return parseResult(result);
            }
        } catch (BaseException e) {
            throw e;
        } catch (IOException e) {
            log.error("大模型接口调用异常", e);
            throw new BaseException("AI 服务暂不可用，请稍后重试");
        }
    }

    private RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(aiProperties.getTimeoutMs())
                .setConnectionRequestTimeout(aiProperties.getTimeoutMs())
                .setSocketTimeout(aiProperties.getTimeoutMs())
                .build();
    }

    /**
     * 构建 chat/completions 请求体
     */
    private String buildRequestBody(String systemPrompt, String userMessage) {
        JSONObject body = new JSONObject();
        body.put("model", aiProperties.getModel());

        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", systemPrompt));
        messages.add(buildMessage("user", userMessage));
        body.put("messages", messages);

        return body.toJSONString();
    }

    /**
     * 构建支持 tools 的请求体
     */
    private String buildRequestBodyWithTools(List<LlmMessage> messages, JSONArray tools) {
        JSONObject body = new JSONObject();
        body.put("model", aiProperties.getModel());

        JSONArray msgArray = new JSONArray();
        for (LlmMessage msg : messages) {
            JSONObject m = new JSONObject();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent() == null ? "" : msg.getContent());
            if ("tool".equals(msg.getRole())) {
                m.put("tool_call_id", msg.getToolCallId());
            }
            // assistant 消息需回传 tool_calls，保证多轮协议正确
            if (msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
                JSONArray toolCalls = new JSONArray();
                for (LlmToolCall call : msg.getToolCalls()) {
                    JSONObject tc = new JSONObject();
                    tc.put("id", call.getId());
                    tc.put("type", "function");
                    JSONObject function = new JSONObject();
                    function.put("name", call.getName());
                    function.put("arguments", call.getArguments() == null ? "{}" : call.getArguments());
                    tc.put("function", function);
                    toolCalls.add(tc);
                }
                m.put("tool_calls", toolCalls);
            }
            msgArray.add(m);
        }
        body.put("messages", msgArray);

        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
        }

        return body.toJSONString();
    }

    private JSONObject buildMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 解析响应，提取 choices[0].message.content
     */
    private String parseAnswer(String json) {
        JSONObject resp = JSON.parseObject(json);
        JSONArray choices = resp.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("大模型返回结构异常: {}", json);
            throw new BaseException("AI 服务返回数据异常");
        }
        return choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    /**
     * 解析响应，提取 content 与 tool_calls
     */
    private LlmChatResult parseResult(String json) {
        JSONObject resp = JSON.parseObject(json);
        JSONArray choices = resp.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("大模型返回结构异常: {}", json);
            throw new BaseException("AI 服务返回数据异常");
        }

        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        LlmChatResult result = new LlmChatResult();
        result.setContent(message.getString("content"));

        JSONArray toolCalls = message.getJSONArray("tool_calls");
        if (toolCalls != null && !toolCalls.isEmpty()) {
            for (int i = 0; i < toolCalls.size(); i++) {
                JSONObject tc = toolCalls.getJSONObject(i);
                JSONObject function = tc.getJSONObject("function");
                LlmToolCall call = new LlmToolCall();
                call.setId(tc.getString("id"));
                if (function != null) {
                    call.setName(function.getString("name"));
                    call.setArguments(function.getString("arguments"));
                }
                result.getToolCalls().add(call);
            }
        }
        return result;
    }
}