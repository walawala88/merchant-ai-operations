package com.merchant.ops.ai.rag.service;

import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.dto.KnowledgeDocumentRequest;
import com.merchant.ops.ai.rag.dto.KnowledgeIngestionResult;
import com.merchant.ops.ai.rag.store.QdrantKnowledgeStore;
import com.merchant.ops.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class KnowledgeIngestionService {

    private static final Set<String> ALLOWED_TYPES = Set.of("SOP", "POLICY");

    private final KnowledgeProperties properties;
    private final KnowledgeChunker chunker;
    private final EmbeddingClient embeddingClient;
    private final QdrantKnowledgeStore store;

    public KnowledgeIngestionService(KnowledgeProperties properties,
                                     KnowledgeChunker chunker,
                                     EmbeddingClient embeddingClient,
                                     QdrantKnowledgeStore store) {
        this.properties = properties;
        this.chunker = chunker;
        this.embeddingClient = embeddingClient;
        this.store = store;
    }

    public KnowledgeIngestionResult ingest(KnowledgeDocumentRequest request) {
        if (!properties.isEnabled()) {
            throw new BaseException("RAG 功能未启用");
        }
        if (!StringUtils.hasText(request.getTenantId())) {
            throw new BaseException("tenantId 不能为空");
        }
        String docType = request.getDocType() == null
                ? "" : request.getDocType().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(docType)) {
            throw new BaseException("docType 只支持 SOP 或 POLICY");
        }
        if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw new BaseException("title 和 content 不能为空");
        }
        String documentId = StringUtils.hasText(request.getDocumentId())
                ? request.getDocumentId() : UUID.randomUUID().toString();
        LocalDate effectiveFrom = parseDate(request.getEffectiveFrom(), LocalDate.now());
        LocalDate effectiveTo = parseDate(request.getEffectiveTo(), LocalDate.now().plusYears(10));
        List<String> chunks = chunker.chunk(request.getContent());
        List<JSONObject> points = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            double[] vector = embeddingClient.embed(chunks.get(i));
            JSONObject payload = new JSONObject();
            payload.put("documentId", documentId);
            payload.put("tenantId", request.getTenantId());
            payload.put("storeId", request.getStoreId());
            payload.put("scope", StringUtils.hasText(request.getStoreId()) ? "STORE" : "GLOBAL");
            payload.put("docType", docType);
            payload.put("title", request.getTitle());
            payload.put("version", StringUtils.hasText(request.getVersion()) ? request.getVersion() : "v1");
            payload.put("effectiveFrom", effectiveFrom.toString());
            payload.put("effectiveTo", effectiveTo.toString());
            payload.put("effectiveFromEpoch", effectiveFrom.toEpochDay());
            payload.put("effectiveToEpoch", effectiveTo.toEpochDay());
            payload.put("sourceUrl", request.getSourceUrl());
            payload.put("text", chunks.get(i));
            payload.put("chunkIndex", i);
            payload.put("status", "ACTIVE");
            JSONObject point = new JSONObject();
            point.put("id", UUID.randomUUID().toString());
            point.put("vector", vector);
            point.put("payload", payload);
            points.add(point);
        }
        // Wait until all embeddings succeed before replacing an existing document.
        store.deleteByDocument(request.getTenantId(), documentId);
        store.upsert(points);
        return new KnowledgeIngestionResult(documentId, chunks.size(), "INDEXED");
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            throw new BaseException("日期格式必须为 yyyy-MM-dd: " + value);
        }
    }
}
