package com.ruoyi.web.controller.common;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.sse.SseEmitterManager;
import com.ruoyi.framework.web.service.TokenService;

@RestController
public class SseController
{
    @Autowired
    private SseEmitterManager sseEmitterManager;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/sse/subscribe")
    public SseEmitter subscribe(HttpServletRequest request)
    {
        String token = request.getParameter("token");
        LoginUser loginUser = null;
        if (StringUtils.isNotEmpty(token))
        {
            loginUser = tokenService.getLoginUserByToken(token);
        }
        if (loginUser == null)
        {
            loginUser = tokenService.getLoginUser(request);
        }
        if (loginUser == null)
        {
            throw new RuntimeException("未登录或token无效");
        }
        return sseEmitterManager.subscribe(loginUser.getUserId());
    }
}
