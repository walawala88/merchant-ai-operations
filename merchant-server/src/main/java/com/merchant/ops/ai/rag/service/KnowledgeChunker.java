package com.merchant.ops.ai.rag.service;

import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeChunker {

    private final KnowledgeProperties properties;

    public KnowledgeChunker(KnowledgeProperties properties) {
        this.properties = properties;
    }

    public List<String> chunk(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        int max = Math.max(200, properties.getChunkSize());
        int overlap = Math.min(Math.max(0, properties.getChunkOverlap()), max / 2);
        String normalized = content.replace("\r\n", "\n").trim();
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + max);
            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf('\n', end);
                if (boundary > start + max / 2) {
                    end = boundary;
                }
            }
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                result.add(chunk);
            }
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(start + 1, end - overlap);
        }
        return result;
    }
}
