package com.merchant.ops.test;

import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.service.KnowledgeChunker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeChunkerTest {

    @Test
    void shouldSplitLongDocumentIntoOverlappingChunks() {
        KnowledgeProperties properties = new KnowledgeProperties();
        properties.setChunkSize(200);
        properties.setChunkOverlap(40);
        KnowledgeChunker chunker = new KnowledgeChunker(properties);
        String content = "退款规则。".repeat(100);

        List<String> chunks = chunker.chunk(content);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allMatch(chunk -> !chunk.isBlank() && chunk.length() <= 200);
    }

    @Test
    void shouldReturnEmptyForBlankContent() {
        KnowledgeChunker chunker = new KnowledgeChunker(new KnowledgeProperties());
        assertThat(chunker.chunk("  ")).isEmpty();
    }
}
