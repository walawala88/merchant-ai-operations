package com.merchant.ops.ai.rag.service;

import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight hybrid reranker: vector similarity + Chinese bigram lexical overlap + title bonus.
 */
@Component
public class KnowledgeReranker {

    private final KnowledgeProperties properties;

    public KnowledgeReranker(KnowledgeProperties properties) {
        this.properties = properties;
    }

    public List<KnowledgeSearchHit> rerank(String query, List<KnowledgeSearchHit> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        Set<String> queryTokens = tokens(query);
        for (KnowledgeSearchHit hit : candidates) {
            Set<String> contentTokens = tokens((hit.getTitle() == null ? "" : hit.getTitle()) + " " + hit.getContent());
            double overlap = queryTokens.isEmpty() ? 0.0
                    : (double) intersection(queryTokens, contentTokens) / queryTokens.size();
            double titleBonus = hit.getTitle() != null && containsAnyToken(queryTokens, tokens(hit.getTitle()))
                    ? 0.08 : 0.0;
            hit.setLexicalScore(round(overlap));
            hit.setFinalScore(round(hit.getVectorScore() * 0.72 + overlap * 0.20 + titleBonus));
        }
        return candidates.stream()
                .sorted(Comparator.comparingDouble(KnowledgeSearchHit::getFinalScore).reversed())
                .limit(Math.max(1, properties.getFinalTopK()))
                .toList();
    }

    private int intersection(Set<String> a, Set<String> b) {
        int count = 0;
        for (String token : a) {
            if (b.contains(token)) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAnyToken(Set<String> query, Set<String> title) {
        for (String token : query) {
            if (title.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        String normalized = text.toLowerCase().replaceAll("[^\\p{IsHan}a-z0-9]", "");
        for (int i = 0; i + 1 < normalized.length(); i++) {
            result.add(normalized.substring(i, i + 2));
        }
        for (String word : text.toLowerCase().split("[^\\p{IsHan}a-z0-9]+")) {
            if (word.length() > 1) {
                result.add(word);
            }
        }
        return result;
    }

    private double round(double value) {
        return Math.round(value * 10000d) / 10000d;
    }
}
