package com.ruoyi.ai.service;

import java.util.List;

import com.ruoyi.ai.domain.AiChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话服务
 */
public interface AiChatService
{
    /**
     * 流式对话：调用大模型，将结果通过 SseEmitter 逐段返回给前端
     *
     * @param message 用户问题
     * @param history 最近对话历史（可空）
     * @param emitter SSE 发射器
     */
    void streamChat(String message, List<AiChatMessage> history, SseEmitter emitter);

    /**
     * 校验并消费限流配额
     *
     * @param ip 客户端 IP
     * @return 是否允许继续
     */
    boolean tryAcquire(String ip);
}
