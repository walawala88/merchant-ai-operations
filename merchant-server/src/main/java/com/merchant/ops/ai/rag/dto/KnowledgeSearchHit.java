package com.merchant.ops.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchHit {
    private String documentId;
    private String title;
    private String docType;
    private String version;
    private String sourceUrl;
    private String effectiveFrom;
    private String effectiveTo;
    private String content;
    private double vectorScore;
    private double lexicalScore;
    private double finalScore;
}
