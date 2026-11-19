package com.merchant.ops.ai.rag.service;

import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import com.merchant.ops.ai.rag.store.QdrantKnowledgeStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KnowledgeRetrievalService {

    private final KnowledgeProperties properties;
    private final EmbeddingClient embeddingClient;
    private final QdrantKnowledgeStore store;
    private final KnowledgeReranker reranker;

    public KnowledgeRetrievalService(KnowledgeProperties properties,
                                     EmbeddingClient embeddingClient,
                                     QdrantKnowledgeStore store,
                                     KnowledgeReranker reranker) {
        this.properties = properties;
        this.embeddingClient = embeddingClient;
        this.store = store;
        this.reranker = reranker;
    }

    public List<KnowledgeSearchHit> search(String tenantId, String storeId, String query, String docType) {
        if (!properties.isEnabled() || !StringUtils.hasText(tenantId) || !StringUtils.hasText(query)) {
            return List.of();
        }
        double[] vector = embeddingClient.embed(query);
        List<KnowledgeSearchHit> candidates = store.search(
                vector, tenantId, storeId, docType, Math.max(properties.getTopK(), properties.getFinalTopK()));
        return reranker.rerank(query, candidates);
    }
}
