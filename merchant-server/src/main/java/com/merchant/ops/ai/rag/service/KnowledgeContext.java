package com.merchant.ops.ai.rag.service;

/**
 * 当前请求的租户和门店上下文。由 AiChatService 注入，工具不能从模型参数中信任租户。
 */
public final class KnowledgeContext {
    private static final ThreadLocal<Scope> HOLDER = new ThreadLocal<>();

    private KnowledgeContext() {
    }

    public static void set(String tenantId, String storeId) {
        HOLDER.set(new Scope(tenantId, storeId));
    }

    public static String tenantId() {
        Scope scope = HOLDER.get();
        return scope == null ? null : scope.tenantId;
    }

    public static String storeId() {
        Scope scope = HOLDER.get();
        return scope == null ? null : scope.storeId;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record Scope(String tenantId, String storeId) {
    }
}
