package com.merchant.ops.unit;

import com.merchant.ops.ai.constant.AiPromptConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptConstantsTest {

    @Test
    void shouldInjectCurrentDateIntoSafetyPrompt() {
        String prompt = AiPromptConstants.buildSystemPrompt();

        assertThat(prompt).contains(LocalDate.now().toString());
        assertThat(prompt).contains("不要凭空编造数据");
        assertThat(prompt).contains("二次确认");
    }

    @Test
    void shouldDetectBusinessAnalysisQuestions() {
        assertThat(AiPromptConstants.isAnalysisQuestion("为什么今天营业额比昨天低")).isTrue();
        assertThat(AiPromptConstants.isAnalysisQuestion("今天有哪些热销菜品")).isFalse();
        assertThat(AiPromptConstants.isAnalysisQuestion(null)).isFalse();
    }
}