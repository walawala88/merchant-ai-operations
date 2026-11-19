package com.merchant.ops.ai.rag.dto;

import lombok.Data;

@Data
public class KnowledgeDocumentRequest {
    private String documentId;
    private String tenantId;
    private String storeId;
    private String docType;
    private String title;
    private String version;
    private String effectiveFrom;
    private String effectiveTo;
    private String sourceUrl;
    private String content;
}
