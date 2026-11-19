package com.merchant.ops.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KnowledgeIngestionResult {
    private String documentId;
    private int chunkCount;
    private String status;
}
