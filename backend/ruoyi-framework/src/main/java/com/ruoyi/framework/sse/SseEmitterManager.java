package com.ruoyi.framework.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.framework.manager.AsyncManager;

@Component
public class SseEmitterManager
{
    private static final Logger log = LoggerFactory.getLogger(SseEmitterManager.class);

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId)
    {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void kickSession(Long userId)
    {
        List<SseEmitter> list = emitters.remove(userId);
        if (list != null)
        {
            AsyncManager.me().execute(new TimerTask()
            {
                @Override
                public void run()
                {
                    for (SseEmitter emitter : list)
                    {
                        try
                        {
                            emitter.send(SseEmitter.event().name("kicked")
                                    .data("您的账户已在其他地方登录，您已被强制下线！"));
                        }
                        catch (IOException e)
                        {
                            log.debug("踢下线推送失败（连接已断开）: userId={}", userId);
                        }
                        finally
                        {
                            try
                            {
                                emitter.complete();
                            }
                            catch (Exception ignored)
                            {
                            }
                        }
                    }
                }
            });
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter)
    {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null)
        {
            list.remove(emitter);
            if (list.isEmpty())
            {
                emitters.remove(userId);
            }
        }
    }
}
