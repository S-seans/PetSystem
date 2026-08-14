package com.ruoyi.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.ruoyi.ai.config.AiConfig;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.pet.service.IPetService;

/**
 * AI 客服限流单元测试：覆盖 Redis 限流主路径与 Redis 不可用时的本地兜底
 */
@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest
{
    @Mock
    private AiConfig aiConfig;

    @Mock
    private IPetService petService;

    @Mock
    private RedisCache redisCache;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @BeforeEach
    void setUp()
    {
        // RedisCache.redisTemplate 为 public 字段，注入 mock 便于测试
        redisCache.redisTemplate = redisTemplate;
    }

    @Test
    @DisplayName("未配置限流（limit<=0）时始终放行")
    void tryAcquire_limitDisabled_alwaysAllowed()
    {
        when(aiConfig.getRateLimit()).thenReturn(0);

        assertTrue(aiChatService.tryAcquire("127.0.0.1"));
        assertTrue(aiChatService.tryAcquire("127.0.0.2"));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("IP 为空时直接放行")
    void tryAcquire_blankIp_allowed()
    {
        when(aiConfig.getRateLimit()).thenReturn(5);

        assertTrue(aiChatService.tryAcquire(""));
        assertTrue(aiChatService.tryAcquire(null));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Redis 限流：窗口内超过次数后拒绝")
    void tryAcquire_redisLimit_exceeded()
    {
        when(aiConfig.getRateLimit()).thenReturn(2);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L, 2L, 3L);

        String ip = "10.0.0.1";
        assertTrue(aiChatService.tryAcquire(ip));
        assertTrue(aiChatService.tryAcquire(ip));
        assertFalse(aiChatService.tryAcquire(ip));

        // 首次计数时设置窗口过期时间
        verify(redisCache).expire(anyString(), anyLong());
    }

    @Test
    @DisplayName("Redis 不可用时回退到本地内存限流")
    void tryAcquire_redisDown_fallbackToLocal()
    {
        when(aiConfig.getRateLimit()).thenReturn(2);
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis connection refused"));

        String ip = "10.0.0.2-local-fallback";
        assertTrue(aiChatService.tryAcquire(ip));
        assertTrue(aiChatService.tryAcquire(ip));
        assertFalse(aiChatService.tryAcquire(ip));
    }
}
