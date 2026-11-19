package com.merchant.ops.ai.rag.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.config.AiProperties;
import com.merchant.ops.ai.rag.config.KnowledgeProperties;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OpenAI-compatible embedding client. The same base URL/API key as the chat model is used.
 */
@Slf4j
@Service
public class EmbeddingClient {

    private final AiProperties aiProperties;
    private final KnowledgeProperties knowledgeProperties;

    public EmbeddingClient(AiProperties aiProperties, KnowledgeProperties knowledgeProperties) {
        this.aiProperties = aiProperties;
        this.knowledgeProperties = knowledgeProperties;
    }

    public double[] embed(String text) {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new BaseException("未配置 AI_API_KEY，无法生成 Embedding");
        }
        JSONObject body = new JSONObject();
        body.put("model", knowledgeProperties.getEmbeddingModel());
        body.put("input", text);
        String url = aiProperties.getBaseUrl() + "/embeddings";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + aiProperties.getApiKey());
            post.setEntity(new StringEntity(body.toJSONString(), ContentType.APPLICATION_JSON));
            post.setConfig(RequestConfig.custom()
                    .setConnectTimeout(aiProperties.getTimeoutMs())
                    .setConnectionRequestTimeout(aiProperties.getTimeoutMs())
                    .setSocketTimeout(aiProperties.getTimeoutMs())
                    .build());
            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("Embedding 调用失败: status={}, body={}", response.getStatusLine().getStatusCode(), responseBody);
                    throw new BaseException("Embedding 服务调用失败");
                }
                JSONObject json = JSON.parseObject(responseBody);
                JSONArray data = json.getJSONArray("data");
                if (data == null || data.isEmpty()) {
                    throw new BaseException("Embedding 服务返回为空");
                }
                JSONArray vector = data.getJSONObject(0).getJSONArray("embedding");
                double[] result = new double[vector.size()];
                for (int i = 0; i < vector.size(); i++) {
                    result[i] = vector.getDoubleValue(i);
                }
                if (result.length != knowledgeProperties.getEmbeddingDimension()) {
                    throw new BaseException("Embedding 维度不匹配: expected "
                            + knowledgeProperties.getEmbeddingDimension() + ", actual " + result.length);
                }
                return result;
            }
        } catch (BaseException ex) {
            throw ex;
        } catch (IOException ex) {
            log.error("Embedding 服务异常", ex);
            throw new BaseException("Embedding 服务暂不可用");
        }
    }
}
