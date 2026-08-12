package com.ruoyi.framework.sse;

/**
 * SSE 订阅认证失败异常，映射为 401 响应
 */
public class SseUnauthorizedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public SseUnauthorizedException(String message)
    {
        super(message);
    }
}
