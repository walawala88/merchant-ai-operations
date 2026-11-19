package com.merchant.ops.ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.constant.AiPromptConstants;
import com.merchant.ops.ai.llm.LlmChatResult;
import com.merchant.ops.ai.llm.LlmClient;
import com.merchant.ops.ai.llm.LlmMessage;
import com.merchant.ops.ai.llm.LlmToolCall;
import com.merchant.ops.ai.rag.service.KnowledgeContext;
import com.merchant.ops.ai.service.AiChatService;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.ai.tool.ToolRegistry;
import com.merchant.ops.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 对话服务实现：Function Calling 编排 + SSE 流式输出。
 */
@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_TOOL_ROUNDS = 5;
    private static final int MAX_HISTORY_SIZE = 40;
    private static final int STREAM_CHUNK_SIZE = 12;
    private static final long STREAM_DELAY_MILLIS = 20L;
    private static final long SSE_TIMEOUT_MILLIS = 180000L;
    private static final String DEFAULT_SESSION = "default";
    private static final String[] CONFIRM_KEYWORDS = {"确认", "确定", "同意", "好的", "可以", "是的", "就这么办"};

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private ThreadPoolTaskExecutor aiStreamExecutor;

    /**
     * 会话历史：sessionId -> 消息列表（含 system/user/assistant/tool）。
     */
    private final Map<String, List<LlmMessage>> sessions = new ConcurrentHashMap<>();

    @Override
    public SseEmitter chatStream(String message, String sessionId, String tenantId, String storeId) {
        if (!StringUtils.hasText(message)) {
            throw new BaseException("消息内容不能为空");
        }
        String sid = StringUtils.hasText(sessionId) ? sessionId : DEFAULT_SESSION;
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        aiStreamExecutor.execute(() -> streamChat(message, sid, tenantId, storeId, emitter));
        return emitter;
    }

    /*
     * 旧的非流式实现已停用，统一由 chatStream 替代。
     * 原逻辑：同步完成多轮工具调用后，一次性返回 AiChatResponse。
     */

    private void streamChat(String message, String sessionId, String tenantId, String storeId, SseEmitter emitter) {
        KnowledgeContext.set(StringUtils.hasText(tenantId) ? tenantId : "default", storeId);
        try {
            sendEvent(emitter, "start", Collections.singletonMap("sessionId", sessionId));

            List<LlmMessage> messages = sessions.computeIfAbsent(sessionId, key -> new ArrayList<>());
            synchronized (messages) {
                if (messages.isEmpty()) {
                    String systemPrompt = AiPromptConstants.isAnalysisQuestion(message)
                            ? AiPromptConstants.buildAnalysisSystemPrompt()
                            : AiPromptConstants.buildSystemPrompt();
                    messages.add(new LlmMessage("system", systemPrompt));
                }
                messages.add(new LlmMessage("user", message));

                boolean needTool = false;
                List<String> usedTools = new ArrayList<>();
                LlmChatResult result = null;

                for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                    sendEvent(emitter, "thinking", Collections.singletonMap("round", round + 1));
                    result = llmClient.chatWithTools(messages, toolRegistry.buildToolDefinitions());

                    if (!result.hasToolCalls()) {
                        break;
                    }

                    needTool = true;
                    LlmMessage assistantMsg = new LlmMessage("assistant", result.getContent());
                    assistantMsg.setToolCalls(result.getToolCalls());
                    messages.add(assistantMsg);

                    for (LlmToolCall call : result.getToolCalls()) {
                        usedTools.add(call.getName());
                        Map<String, Object> toolRunning = new LinkedHashMap<>();
                        toolRunning.put("name", call.getName());
                        toolRunning.put("status", "running");
                        sendEvent(emitter, "tool", toolRunning);

                        String toolResult = executeToolCall(call, message);
                        Map<String, Object> toolFinished = new LinkedHashMap<>();
                        toolFinished.put("name", call.getName());
                        toolFinished.put("status", toolResult.contains("\"error\"") ? "error" : "done");
                        sendEvent(emitter, "tool_result", toolFinished);

                        LlmMessage toolMsg = new LlmMessage("tool", toolResult);
                        toolMsg.setToolCallId(call.getId());
                        messages.add(toolMsg);
                    }
                }

                if (result == null) {
                    throw new BaseException("AI 服务未返回结果");
                }

                trimHistory(messages);
                String answer = StringUtils.hasText(result.getContent())
                        ? result.getContent()
                        : "暂未生成回答，请稍后重试。";

                int index = 0;
                for (String chunk : splitAnswer(answer)) {
                    Map<String, Object> token = new LinkedHashMap<>();
                    token.put("content", chunk);
                    token.put("index", index++);
                    sendEvent(emitter, "message", token);
                    sleepBetweenChunks();
                }

                Map<String, Object> done = new LinkedHashMap<>();
                done.put("intent", needTool ? "TOOL_CALL" : "CHAT");
                done.put("needTool", needTool);
                done.put("toolName", String.join(",", usedTools));
                sendEvent(emitter, "done", done);
            }
            emitter.complete();
        } catch (Exception ex) {
            log.error("AI SSE 流式响应失败", ex);
            sendErrorAndComplete(emitter, ex);
        } finally {
            KnowledgeContext.clear();
        }
    }

    private String executeToolCall(LlmToolCall call, String userMessage) {
        AiTool tool = toolRegistry.get(call.getName());
        if (tool == null) {
            return JSON.toJSONString(Collections.singletonMap("error", "未找到工具: " + call.getName()));
        }
        if (tool.isWriteOperation() && !isUserConfirmed(userMessage, call.getArguments())) {
            return JSON.toJSONString(Collections.singletonMap("error",
                    "该修改操作需要用户明确确认。请先展示操作预览，征得用户同意后再携带确认令牌执行。"));
        }
        try {
            return tool.execute(call.getArguments());
        } catch (Exception ex) {
            log.error("工具执行失败: {}", call.getName(), ex);
            return JSON.toJSONString(Collections.singletonMap("error", "工具执行失败: " + ex.getMessage()));
        }
    }

    private boolean isUserConfirmed(String userMessage, String arguments) {
        if (!StringUtils.hasText(arguments)) {
            return true;
        }
        try {
            JSONObject args = JSON.parseObject(arguments);
            if (!StringUtils.hasText(args.getString("token"))) {
                return true;
            }
        } catch (Exception ex) {
            return true;
        }
        return containsAny(userMessage, CONFIRM_KEYWORDS);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text != null && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void trimHistory(List<LlmMessage> messages) {
        if (messages.size() <= MAX_HISTORY_SIZE) {
            return;
        }
        List<LlmMessage> kept = new ArrayList<>();
        kept.add(messages.get(0));
        kept.addAll(messages.subList(messages.size() - (MAX_HISTORY_SIZE - 1), messages.size()));
        messages.clear();
        messages.addAll(kept);
    }

    private List<String> splitAnswer(String answer) {
        int[] codePoints = answer.codePoints().toArray();
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < codePoints.length; i += STREAM_CHUNK_SIZE) {
            int length = Math.min(STREAM_CHUNK_SIZE, codePoints.length - i);
            chunks.add(new String(codePoints, i, length));
        }
        return chunks;
    }

    private void sleepBetweenChunks() {
        try {
            Thread.sleep(STREAM_DELAY_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BaseException("SSE 流式输出被中断");
        }
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object payload) throws IOException {
        emitter.send(SseEmitter.event()
                .name(eventName)
                .data(payload, MediaType.APPLICATION_JSON));
    }

    private void sendErrorAndComplete(SseEmitter emitter, Exception ex) {
        try {
            sendEvent(emitter, "error", Collections.singletonMap("message", ex.getMessage()));
            emitter.complete();
        } catch (Exception sendEx) {
            emitter.completeWithError(sendEx);
        }
    }
}
