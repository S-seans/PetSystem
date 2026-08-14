package com.ruoyi.ai.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.domain.AiChatMessage;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.service.AiChatService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能客服Controller
 *
 * 匿名可用：未登录用户也能在公开页面使用 AI 对话。
 */
@RestController
@RequestMapping("/ai")
public class AiChatController extends BaseController
{
    @Autowired
    private AiChatService aiChatService;

    /**
     * 流式对话（SSE）
     */
    @Anonymous
    @PostMapping("/chat/stream")
    public SseEmitter stream(@RequestBody AiChatRequest request, HttpServletRequest httpServletRequest)
    {
        SseEmitter emitter = new SseEmitter(120000L);

        String message = request == null ? null : request.getMessage();
        if (StringUtils.isBlank(message))
        {
            sendJson(emitter, "error", "问题不能为空");
            return emitter;
        }

        // 限流校验
        String ip = IpUtils.getIpAddr(httpServletRequest);
        if (!aiChatService.tryAcquire(ip))
        {
            sendJson(emitter, "error", "提问过于频繁，请稍后再试");
            return emitter;
        }

        List<AiChatMessage> history = request.getHistory();
        // WebClient 流式调用为异步非阻塞，无需手工创建线程
        aiChatService.streamChat(message, history, emitter);
        return emitter;
    }

    private void sendJson(SseEmitter emitter, String type, String content)
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("content", content);
            emitter.send(SseEmitter.event().data(obj.toJSONString()));
            emitter.complete();
        }
        catch (Exception e)
        {
            logger.error("SSE 发送失败", e);
        }
    }
}
