package com.ruoyi.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 客服配置
 * 
 * 读取 application.yml 中 ai.* 配置项。
 */
@Component
public class AiConfig
{
    /** 总开关 */
    @Value("${ai.enabled:true}")
    private boolean enabled;

    /** 接口地址（OpenAI 兼容） */
    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    /** API Key，留空则功能禁用 */
    @Value("${ai.api-key:}")
    private String apiKey;

    /** 模型名称 */
    @Value("${ai.model:deepseek-chat}")
    private String model;

    /** 调用超时（秒） */
    @Value("${ai.timeout:60}")
    private int timeout;

    /** 单 IP 限流（次/分钟），0 表示不限 */
    @Value("${ai.rate-limit:10}")
    private int rateLimit;

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public String getModel()
    {
        return model;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public int getRateLimit()
    {
        return rateLimit;
    }

    /**
     * 是否可用：开关开启且已配置 API Key
     */
    public boolean isAvailable()
    {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }
}
