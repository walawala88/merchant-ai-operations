package com.merchant.ops.test;

import com.merchant.ops.ai.rag.config.KnowledgeProperties;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import com.merchant.ops.ai.rag.service.KnowledgeReranker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeRerankerTest {

    @Test
    void shouldPreferRelevantPolicyAfterHybridRerank() {
        KnowledgeProperties properties = new KnowledgeProperties();
        properties.setFinalTopK(2);
        KnowledgeReranker reranker = new KnowledgeReranker(properties);
        KnowledgeSearchHit unrelated = KnowledgeSearchHit.builder()
                .title("门店闭店检查")
                .content("检查灯光和库存")
                .vectorScore(0.8)
                .build();
        KnowledgeSearchHit relevant = KnowledgeSearchHit.builder()
                .title("退款规则")
                .content("顾客在商家接单前申请取消，可以按平台流程退款")
                .vectorScore(0.7)
                .build();

        List<KnowledgeSearchHit> result = reranker.rerank("退款需要满足什么条件", List.of(unrelated, relevant));

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).isEqualTo("退款规则");
        assertThat(result.get(0).getFinalScore()).isGreaterThan(result.get(1).getFinalScore());
    }
}
