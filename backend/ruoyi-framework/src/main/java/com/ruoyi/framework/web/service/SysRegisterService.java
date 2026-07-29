package com.ruoyi.framework.web.service;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Transactional
    public String register(RegisterBody registerBody)
    {
        String nickname = registerBody.getNickname();
        String password = registerBody.getPassword();

        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(nickname, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(nickname))
        {
            throw new ServiceException("昵称不能为空");
        }
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("密码不能为空");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        String account = generateUniqueAccount();

        SysUser sysUser = new SysUser();
        sysUser.setUserName(account);
        sysUser.setNickName(nickname);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));

        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag)
        {
            throw new ServiceException("注册失败,请联系系统管理人员");
        }

        userService.insertUserAuth(sysUser.getUserId(), new Long[]{100L});

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(account, Constants.REGISTER,
                MessageUtils.message("user.register.success")));

        return account;
    }

    private String generateUniqueAccount()
    {
        SecureRandom random = new SecureRandom();
        String account;
        int maxAttempts = 100;
        do
        {
            account = String.valueOf(1000000 + random.nextInt(9000000));
            if (--maxAttempts <= 0)
            {
                throw new ServiceException("无法生成唯一账号，请稍后重试");
            }
        } while (!checkAccountUnique(account));
        return account;
    }

    private boolean checkAccountUnique(String account)
    {
        SysUser user = new SysUser();
        user.setUserName(account);
        return userService.checkUserNameUnique(user);
    }

    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}
