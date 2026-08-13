package com.ruoyi.ai.domain;

/**
 * AI 对话消息
 */
public class AiChatMessage
{
    /** 角色：system / user / assistant */
    private String role;

    /** 内容 */
    private String content;

    public AiChatMessage()
    {
    }

    public AiChatMessage(String role, String content)
    {
        this.role = role;
        this.content = content;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
