package com.merchant.ops.ai.rag.dto;

import lombok.Data;

@Data
public class KnowledgeSearchRequest {
    private String tenantId;
    private String storeId;
    private String query;
    private String docType;
    private Integer topK;
}
