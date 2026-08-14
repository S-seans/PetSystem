package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.AiConfig;
import com.ruoyi.ai.domain.AiChatMessage;
import com.ruoyi.ai.service.AiChatService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pet.constant.PetGender;
import com.ruoyi.pet.domain.Pet;
import com.ruoyi.pet.service.IPetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * AI 对话服务实现
 * 
 * 流程：组装 System Prompt（系统功能 + 领养规则）→ 宠物数据动态检索拼入上下文 →
 * 调用 DeepSeek（OpenAI 兼容 /chat/completions，stream=true）→ 逐 token 转发给前端。
 * 
 * 说明：使用 WebClient（Reactive）流式消费 SSE，避免手工线程与阻塞 IO；
 * 限流优先使用 Redis（多实例共享），Redis 不可用时自动回退到本地内存限流。
 */
@Service
public class AiChatServiceImpl implements AiChatService
{
    private static final Logger logger = LoggerFactory.getLogger(AiChatServiceImpl.class);

    /** 本地限流缓存：ip -> 请求时间戳列表（仅作为 Redis 不可用时的兜底） */
    private static final Map<String, List<Long>> RATE_CACHE = new ConcurrentHashMap<>();

    /** Redis 限流键前缀 */
    private static final String RATE_KEY_PREFIX = "ai:rate:";

    /** 宠物相关关键词，命中则触发宠物数据检索 */
    private static final String[] PET_KEYWORDS = { "宠物", "猫", "狗", "兔", "鼠", "仓鼠", "领养", "毛孩子", "汪", "喵" };

    /** 常用品种关键词，用于定向检索 */
    private static final String[] BREED_KEYWORDS = { "橘猫", "狸花", "布偶", "英短", "蓝猫", "金渐层", "美短", "暹罗", "波斯", "折耳",
            "无毛猫", "缅因", "金毛", "拉布拉多", "哈士奇", "萨摩", "柯基", "博美", "泰迪", "贵宾", "边牧", "德牧", "比熊", "法斗", "柴犬",
            "中华田园猫", "中华田园犬", "垂耳兔", "侏儒兔", "安哥拉兔" };

    /** 每次返回的最大宠物条目数 */
    private static final int MAX_PET_ITEMS = 20;

    /** 携带的历史消息条数上限 */
    private static final int MAX_HISTORY = 10;

    /** 限流窗口（毫秒） */
    private static final long RATE_WINDOW_MS = 60_000L;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private IPetService petService;

    @Autowired
    private RedisCache redisCache;

    /** WebClient 实例（懒初始化，线程安全） */
    private volatile WebClient webClient;

    /**
     * 系统功能说明（静态 Prompt）
     */
    private static final String SYSTEM_PROMPT =
            "你是\"爱心宠物领养平台\"（动物救助站宠物领养管理系统）的智能客服助手。\n"
                    + "请用简洁、友好、口语化的中文回答用户问题；若问题超出本系统范围，请说明你只了解本系统。\n"
                    + "回答宠物相关问题时，请优先参考提供的【系统当前宠物数据】，据此给出准确信息（如具体宠物名、品种、状态），"
                    + "不要编造系统中不存在的数据；数据未提供的细节可诚实说明。\n\n"
                    + "## 系统功能\n"
                    + "1. 宠物管理：工作人员录入、编辑、查询宠物信息（名称、品种、年龄、性别、体重、照片、状况描述、救助日期、状态）。\n"
                    + "2. 领养申请：用户浏览\"可领养\"宠物后提交领养申请（填写领养理由），可随时撤销自己的\"待审核\"申请。\n"
                    + "3. 健康档案：记录宠物的疫苗、绝育、健康状况等。\n"
                    + "4. 成功故事：展示已领养成功宠物的故事。\n\n"
                    + "## 领养流程\n"
                    + "1. 注册登录后即可申请；未登录可浏览公开宠物列表。\n"
                    + "2. 在宠物展示页（/adopt/public）浏览\"可领养\"的宠物。\n"
                    + "3. 点击\"申请领养\"进入申请页，填写领养理由并提交。\n"
                    + "4. 管理员审核：待审核(pending) → 通过(pass) / 拒绝(reject)。\n"
                    + "5. 审核通过后办理领养手续，宠物状态更新为\"已领养\"，并生成成功故事记录。\n"
                    + "6. 用户可在\"我的领养申请\"页面查看进度，并可撤销待审核的申请。\n\n"
                    + "## 角色权限\n"
                    + "- 游客：可浏览公开宠物列表；提交领养申请需先登录。\n"
                    + "- 普通用户：浏览宠物、提交/撤销/修改自己的待审核申请、管理个人资料。\n"
                    + "- 管理员：审核领养申请、管理宠物信息、健康档案与成功故事。";

    @Override
    public void streamChat(String message, List<AiChatMessage> history, SseEmitter emitter)
    {
        if (!aiConfig.isAvailable())
        {
            sendEvent(emitter, "error", "AI 服务未配置，请联系管理员在 application.yml 中填写 ai.api-key");
            emitter.complete();
            return;
        }
        if (StringUtils.isBlank(message))
        {
            sendEvent(emitter, "error", "问题不能为空");
            emitter.complete();
            return;
        }

        try
        {
            String petContext = buildPetContext(message);
            List<AiChatMessage> messages = buildMessages(message, history, petContext);
            callDeepSeekStream(messages, emitter);
        }
        catch (Exception e)
        {
            logger.error("AI 对话失败", e);
            sendEvent(emitter, "error", "AI 服务暂时不可用，请稍后再试");
            emitter.complete();
        }
    }

    /**
     * 校验并消费限流配额（优先 Redis，失败回退本地内存）
     *
     * @param ip 客户端 IP
     * @return 是否允许继续
     */
    public boolean tryAcquire(String ip)
    {
        int limit = aiConfig.getRateLimit();
        if (limit <= 0 || StringUtils.isBlank(ip))
        {
            return true;
        }
        try
        {
            return tryAcquireRedis(ip, limit);
        }
        catch (Exception e)
        {
            logger.warn("Redis 限流不可用，回退到本地内存限流：{}", e.getMessage());
            return tryAcquireLocal(ip, limit);
        }
    }

    /**
     * Redis 计数限流（INCR + 过期窗口），多实例共享
     */
    private boolean tryAcquireRedis(String ip, int limit)
    {
        String key = RATE_KEY_PREFIX + ip;
        Long count = redisCache.redisTemplate.opsForValue().increment(key, 1L);
        if (count != null && count == 1L)
        {
            // 首次计数时设置过期时间（秒）
            redisCache.expire(key, RATE_WINDOW_MS / 1000);
        }
        return count == null || count <= limit;
    }

    /**
     * 本地内存滑动窗口限流（Redis 不可用时的兜底）
     */
    private boolean tryAcquireLocal(String ip, int limit)
    {
        long now = System.currentTimeMillis();
        synchronized (RATE_CACHE)
        {
            List<Long> timestamps = RATE_CACHE.computeIfAbsent(ip, k -> new LinkedList<>());
            timestamps.removeIf(t -> now - t > RATE_WINDOW_MS);
            if (timestamps.size() >= limit)
            {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }

    /**
     * 组装发送给大模型的消息列表：system(prompt+宠物上下文) + 最近历史 + 当前问题
     */
    private List<AiChatMessage> buildMessages(String message, List<AiChatMessage> history, String petContext)
    {
        List<AiChatMessage> messages = new ArrayList<>();
        String system = SYSTEM_PROMPT;
        if (StringUtils.isNotEmpty(petContext))
        {
            system += "\n\n" + petContext;
        }
        messages.add(new AiChatMessage("system", system));

        if (history != null && !history.isEmpty())
        {
            int from = Math.max(0, history.size() - MAX_HISTORY);
            for (int i = from; i < history.size(); i++)
            {
                AiChatMessage m = history.get(i);
                if (m != null && m.getRole() != null && m.getContent() != null)
                {
                    messages.add(m);
                }
            }
        }
        messages.add(new AiChatMessage("user", message));
        return messages;
    }

    /**
     * 宠物数据动态检索：命中宠物关键词时查库，将宠物信息格式化为上下文文本
     */
    private String buildPetContext(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return "";
        }
        boolean petRelated = false;
        for (String kw : PET_KEYWORDS)
        {
            if (message.contains(kw))
            {
                petRelated = true;
                break;
            }
        }
        if (!petRelated)
        {
            return "";
        }

        String breed = extractBreedKeyword(message);
        List<Pet> pets = new ArrayList<>();
        if (breed != null)
        {
            Pet query = new Pet();
            query.setBreed(breed);
            pets = petService.selectPublicPetList(query);
            if (pets == null || pets.isEmpty())
            {
                query = new Pet();
                query.setName(breed);
                pets = petService.selectPublicPetList(query);
            }
        }
        if (pets == null || pets.isEmpty())
        {
            pets = petService.selectPublicPetList(new Pet());
        }
        if (pets == null || pets.isEmpty())
        {
            return "";
        }

        StringBuilder sb = new StringBuilder("【系统当前宠物数据】");
        int count = 0;
        for (Pet p : pets)
        {
            if (count >= MAX_PET_ITEMS)
            {
                break;
            }
            sb.append("\n- ").append(p.getName());
            sb.append("，品种：").append(StringUtils.defaultIfEmpty(p.getBreed(), "未知"));
            sb.append("，性别：").append(PetGender.MALE.equals(p.getGender()) ? "公" : "母");
            sb.append("，年龄：").append(formatPetAge(p.getAge()));
            sb.append("，体重：").append(p.getWeight() == null ? "?" : p.getWeight()).append("kg");
            sb.append("，状态：").append(StringUtils.defaultIfEmpty(p.getStatus(), "未知"));
            if (StringUtils.isNotEmpty(p.getDescription()))
            {
                sb.append("，描述：").append(p.getDescription());
            }
            count++;
        }
        return sb.toString();
    }

    /**
     * 从问题中提取品种关键词（命中则定向检索）
     */
    private String extractBreedKeyword(String message)
    {
        for (String breed : BREED_KEYWORDS)
        {
            if (message.contains(breed))
            {
                return breed;
            }
        }
        return null;
    }

    /**
     * 将宠物年龄（月）格式化为人类可读描述
     */
    private String formatPetAge(Long months)
    {
        if (months == null)
        {
            return "未知";
        }
        long m = months;
        if (m <= 0)
        {
            return "不足1个月";
        }
        long years = m / 12;
        long rest = m % 12;
        if (years == 0)
        {
            return rest + "个月";
        }
        return rest == 0 ? years + "岁" : years + "岁" + rest + "个月";
    }

    /**
     * 懒初始化 WebClient
     * 说明：不设置 responseTimeout（Spring 5.3 不支持且会掐断长 SSE 流），
     * 整体超时由 SseEmitter 的 120s 超时 + onTimeout 取消订阅兜底。
     */
    private WebClient getWebClient()
    {
        WebClient client = this.webClient;
        if (client == null)
        {
            synchronized (this)
            {
                client = this.webClient;
                if (client == null)
                {
                    client = WebClient.builder().build();
                    this.webClient = client;
                }
            }
        }
        return client;
    }

    /**
     * 调用 DeepSeek 流式接口并逐 token 转发（Reactive，无手工线程）
     */
    private void callDeepSeekStream(List<AiChatMessage> messages, SseEmitter emitter)
    {
        String baseUrl = aiConfig.getBaseUrl();
        if (!baseUrl.endsWith("/"))
        {
            baseUrl += "/";
        }
        String urlStr = baseUrl + "chat/completions";

        JSONObject body = new JSONObject();
        body.put("model", aiConfig.getModel());
        body.put("stream", true);
        body.put("temperature", 0.7);
        JSONArray messageArr = new JSONArray();
        for (AiChatMessage m : messages)
        {
            JSONObject jo = new JSONObject();
            jo.put("role", m.getRole());
            jo.put("content", m.getContent());
            messageArr.add(jo);
        }
        body.put("messages", messageArr);

        Flux<ServerSentEvent<String>> stream = getWebClient().post()
                .uri(urlStr)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.getApiKey())
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body.toJSONString())
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>()
                {
                });

        // 持有订阅句柄，便于客户端断开/超时/出错时取消上游请求
        AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable disposable = stream.subscribe(
                event -> handleSseEvent(event, emitter),
                error -> {
                    logger.error("DeepSeek 流式调用失败：{}", error.getMessage());
                    sendEvent(emitter, "error", "模型服务调用失败，请稍后再试");
                    emitter.complete();
                },
                () -> {
                    sendEvent(emitter, "done", null);
                    emitter.complete();
                });
        disposableRef.set(disposable);

        // 客户端断开 / 超时 / 异常时取消上游订阅，避免资源泄漏
        emitter.onCompletion(() -> disposeQuietly(disposableRef));
        emitter.onTimeout(() -> disposeQuietly(disposableRef));
        emitter.onError(t -> {
            logger.warn("SSE 连接异常：{}", t.getMessage());
            disposeQuietly(disposableRef);
        });
    }

    private void disposeQuietly(AtomicReference<Disposable> disposableRef)
    {
        Disposable disposable = disposableRef.get();
        if (disposable != null && !disposable.isDisposed())
        {
            disposable.dispose();
        }
    }

    /**
     * 处理一条 DeepSeek SSE 事件（data: {...} 或 data: [DONE]）
     */
    private void handleSseEvent(ServerSentEvent<String> event, SseEmitter emitter)
    {
        String payload = event == null ? null : event.data();
        if (StringUtils.isBlank(payload))
        {
            return;
        }
        String data = payload.trim();
        if ("[DONE]".equals(data))
        {
            return;
        }
        try
        {
            JSONObject obj = JSON.parseObject(data);
            if (obj == null || obj.get("error") != null)
            {
                logger.warn("DeepSeek 流式数据异常：{}", data);
                sendEvent(emitter, "error", "模型服务调用失败，请稍后再试");
                emitter.complete();
                return;
            }
            JSONArray choices = obj.getJSONArray("choices");
            if (choices == null || choices.isEmpty())
            {
                return;
            }
            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
            if (delta == null)
            {
                return;
            }
            String content = delta.getString("content");
            if (StringUtils.isEmpty(content))
            {
                return;
            }
            if (!sendEvent(emitter, "content", content))
            {
                logger.warn("客户端已断开连接");
            }
        }
        catch (Exception e)
        {
            logger.warn("DeepSeek 流式数据解析失败：{}", e.getMessage());
        }
    }

    /**
     * 发送一条 SSE 消息：{"type":"...","content":"..."}
     */
    private boolean sendEvent(SseEmitter emitter, String type, String content)
    {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        if (content != null)
        {
            obj.put("content", content);
        }
        try
        {
            emitter.send(SseEmitter.event().data(obj.toJSONString()));
            return true;
        }
        catch (Exception e)
        {
            logger.warn("SSE 发送失败：{}", e.getMessage());
            return false;
        }
    }
}
