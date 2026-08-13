package com.ruoyi.ai.domain;

import java.util.List;

/**
 * AI 对话请求
 */
public class AiChatRequest
{
    /** 用户当前问题 */
    private String message;

    /** 最近对话历史（可空，assistant/user 交替） */
    private List<AiChatMessage> history;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<AiChatMessage> getHistory()
    {
        return history;
    }

    public void setHistory(List<AiChatMessage> history)
    {
        this.history = history;
    }
}
