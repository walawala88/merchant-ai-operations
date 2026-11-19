package com.merchant.ops.ai.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "merchant.rag")
public class KnowledgeProperties {
    private boolean enabled = true;
    private String qdrantUrl = "http://127.0.0.1:6333";
    private String collection = "merchant_knowledge";
    private String embeddingModel = "embedding-3";
    private int embeddingDimension = 1024;
    private int topK = 12;
    private int finalTopK = 5;
    private double minScore = 0.2;
    private int chunkSize = 500;
    private int chunkOverlap = 80;
}
