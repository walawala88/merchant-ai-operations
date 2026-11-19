package com.merchant.ops.ai.rag.store;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import com.merchant.ops.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Qdrant REST vector store. Qdrant provides HNSW ANN search; payload filters isolate tenants and stores.
 */
@Slf4j
@Component
public class QdrantKnowledgeStore {

    private final KnowledgeProperties properties;
    private final HttpClient httpClient;
    private volatile boolean initialized;

    public QdrantKnowledgeStore(KnowledgeProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public synchronized void ensureCollection() {
        if (initialized) {
            return;
        }
        try {
            JsonResponse existing = send("GET", "/collections/" + properties.getCollection(), null);
            if (existing.status == 404) {
                JSONObject vectors = new JSONObject();
                vectors.put("size", properties.getEmbeddingDimension());
                vectors.put("distance", "Cosine");
                JSONObject hnsw = new JSONObject();
                hnsw.put("m", 16);
                hnsw.put("ef_construct", 100);
                JSONObject body = new JSONObject();
                body.put("vectors", vectors);
                body.put("hnsw_config", hnsw);
                JsonResponse created = send("PUT", "/collections/" + properties.getCollection(), body);
                requireSuccess(created, "创建 Qdrant collection");
            } else if (existing.status >= 300) {
                throw new BaseException("Qdrant 不可用: " + existing.status + " " + existing.body);
            }
            createPayloadIndex("tenantId", "keyword");
            createPayloadIndex("storeId", "keyword");
            createPayloadIndex("scope", "keyword");
            createPayloadIndex("docType", "keyword");
            createPayloadIndex("status", "keyword");
            createPayloadIndex("effectiveFromEpoch", "integer");
            createPayloadIndex("effectiveToEpoch", "integer");
            initialized = true;
        } catch (BaseException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("初始化 Qdrant 失败", ex);
            throw new BaseException("Qdrant 初始化失败: " + ex.getMessage());
        }
    }

    public void upsert(List<JSONObject> points) {
        ensureCollection();
        JSONObject body = new JSONObject();
        body.put("points", points);
        JsonResponse response = send("PUT", "/collections/" + properties.getCollection() + "/points?wait=true", body);
        requireSuccess(response, "写入 Qdrant 向量");
    }

    public void deleteByDocument(String tenantId, String documentId) {
        ensureCollection();
        JSONArray must = new JSONArray();
        must.add(match("tenantId", tenantId));
        must.add(match("documentId", documentId));
        JSONObject filter = new JSONObject();
        filter.put("must", must);
        JSONObject body = new JSONObject();
        body.put("filter", filter);
        JsonResponse response = send("POST", "/collections/" + properties.getCollection() + "/points/delete?wait=true", body);
        requireSuccess(response, "删除 Qdrant 文档向量");
    }

    public List<KnowledgeSearchHit> search(double[] vector, String tenantId, String storeId,
                                           String docType, int topK) {
        ensureCollection();
        JSONArray must = new JSONArray();
        must.add(match("tenantId", tenantId));
        must.add(match("status", "ACTIVE"));
        JSONObject filter = new JSONObject();
        if (storeId != null && !storeId.isBlank()) {
            JSONArray should = new JSONArray();
            should.add(match("scope", "GLOBAL"));
            should.add(match("storeId", storeId));
            filter.put("should", should);
        }
        if (docType != null && !docType.isBlank() && !"ALL".equalsIgnoreCase(docType)) {
            must.add(match("docType", docType.toUpperCase()));
        }
        long today = LocalDate.now().toEpochDay();
        must.add(range("effectiveFromEpoch", "lte", today));
        must.add(range("effectiveToEpoch", "gte", today));
        filter.put("must", must);

        JSONObject params = new JSONObject();
        params.put("hnsw_ef", 64);
        JSONObject body = new JSONObject();
        body.put("vector", vector);
        body.put("limit", topK);
        body.put("with_payload", true);
        body.put("score_threshold", properties.getMinScore());
        body.put("params", params);
        body.put("filter", filter);

        JsonResponse response = send("POST", "/collections/" + properties.getCollection() + "/points/search", body);
        requireSuccess(response, "Qdrant ANN 检索");
        JSONArray results = JSON.parseObject(response.body).getJSONArray("result");
        List<KnowledgeSearchHit> hits = new ArrayList<>();
        if (results == null) {
            return hits;
        }
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            JSONObject payload = item.getJSONObject("payload");
            if (payload == null) {
                continue;
            }
            hits.add(KnowledgeSearchHit.builder()
                    .documentId(payload.getString("documentId"))
                    .title(payload.getString("title"))
                    .docType(payload.getString("docType"))
                    .version(payload.getString("version"))
                    .sourceUrl(payload.getString("sourceUrl"))
                    .effectiveFrom(payload.getString("effectiveFrom"))
                    .effectiveTo(payload.getString("effectiveTo"))
                    .content(payload.getString("text"))
                    .vectorScore(item.getDoubleValue("score"))
                    .build());
        }
        return hits;
    }

    private void createPayloadIndex(String fieldName, String fieldSchema) {
        JSONObject body = new JSONObject();
        body.put("field_name", fieldName);
        body.put("field_schema", fieldSchema);
        JsonResponse response = send("PUT",
                "/collections/" + properties.getCollection() + "/index?wait=true", body);
        if (response.status >= 300 && response.status != 409) {
            log.warn("创建 Qdrant payload index 失败: field={}, status={}, body={}",
                    fieldName, response.status, response.body);
        }
    }

    private JSONObject match(String key, String value) {
        JSONObject result = new JSONObject();
        result.put("key", key);
        JSONObject match = new JSONObject();
        match.put("value", value);
        result.put("match", match);
        return result;
    }

    private JSONObject range(String key, String operator, long value) {
        JSONObject result = new JSONObject();
        result.put("key", key);
        JSONObject range = new JSONObject();
        range.put(operator, value);
        result.put("range", range);
        return result;
    }

    private JsonResponse send(String method, String path, JSONObject body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(trimBaseUrl() + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .method(method, body == null
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(body.toJSONString()));
            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return new JsonResponse(response.statusCode(), response.body());
        } catch (Exception ex) {
            throw new BaseException("Qdrant 请求失败: " + ex.getMessage());
        }
    }

    private void requireSuccess(JsonResponse response, String action) {
        if (response.status < 200 || response.status >= 300) {
            throw new BaseException(action + "失败: status=" + response.status + ", body=" + response.body);
        }
    }

    private String trimBaseUrl() {
        String url = properties.getQdrantUrl();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private record JsonResponse(int status, String body) {
    }
}
