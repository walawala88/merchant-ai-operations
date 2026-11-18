package com.merchant.ops.ai.controller;

import com.merchant.ops.ai.dto.AiChatRequest;
import com.merchant.ops.ai.service.AiChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话（管理端，SSE 流式输出）。
 */
@RestController
@RequestMapping("/admin/ai")
@Slf4j
@Api(tags = "AI 对话相关接口")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("AI 对话（SSE 流式）")
    public SseEmitter chatStream(@RequestBody AiChatRequest aiChatRequest) {
        log.info("AI 流式对话请求: sessionId={}, message={}",
                aiChatRequest.getSessionId(), aiChatRequest.getMessage());
        return aiChatService.chatStream(aiChatRequest.getMessage(), aiChatRequest.getSessionId());
    }

    /*
     * 旧的非流式接口已停用，保留代码位置便于回滚：
     *
     * @PostMapping("/chat")
     * public Result<AiChatResponse> chat(@RequestBody AiChatRequest aiChatRequest) {
     *     AiChatResponse response = aiChatService.chat(
     *             aiChatRequest.getMessage(), aiChatRequest.getSessionId());
     *     return Result.success(response);
     * }
     */
}
