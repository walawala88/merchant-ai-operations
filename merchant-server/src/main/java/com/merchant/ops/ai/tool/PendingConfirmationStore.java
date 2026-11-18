package com.merchant.ops.ai.tool;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 修改型工具二次确认的令牌存储（由 Java 后端维护，LLM 不可见）
 * <p>
 * 流程：修改型工具首次被调用时生成 token 并存入此处返回给 LLM，
 * 用户确认后 LLM 携带 token 再次调用，后端校验通过才真正执行修改。
 * 这样 LLM 无法自行确认，必须由用户明确确认后由 Java 校验放行。
 */
@Component
public class PendingConfirmationStore {

    /**
     * token -> 待确认操作描述
     */
    private final Map<String, String> pendingOperations = new ConcurrentHashMap<>();

    /**
     * 生成并保存一个新的待确认操作
     *
     * @param description 操作描述（如"停售：宫保鸡丁"）
     * @return 生成的确认令牌
     */
    public String create(String description) {
        String token = java.util.UUID.randomUUID().toString();
        pendingOperations.put(token, description);
        return token;
    }

    /**
     * 校验并消费确认令牌（校验成功即移除，令牌一次性）
     *
     * @param token       用户确认后携带的令牌
     * @param description 期望的操作描述，必须与创建时一致
     * @return true=校验通过，false=令牌无效、已过期或描述不匹配
     */
    public boolean validateAndConsume(String token, String description) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String saved = pendingOperations.get(token);
        if (saved == null || !saved.equals(description)) {
            return false;
        }
        pendingOperations.remove(token);
        return true;
    }
}