package com.merchant.ops.unit;

import com.merchant.ops.ai.tool.PendingConfirmationStore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PendingConfirmationStoreTest {

    private final PendingConfirmationStore store = new PendingConfirmationStore();

    @Test
    void shouldConsumeTokenOnlyOnce() {
        String token = store.create("STOP_DISH:1");

        assertThat(store.validateAndConsume(token, "STOP_DISH:1")).isTrue();
        assertThat(store.validateAndConsume(token, "STOP_DISH:1")).isFalse();
    }

    @Test
    void shouldRejectMismatchedOperation() {
        String token = store.create("STOP_DISH:1");

        assertThat(store.validateAndConsume(token, "STOP_DISH:2")).isFalse();
        assertThat(store.validateAndConsume(token, "STOP_DISH:1")).isTrue();
    }
}