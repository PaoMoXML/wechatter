package com.xmlin.wechatter.wechatbot.chatgpt.baidu;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatBot;
import com.xmlin.wechatter.wechatbot.chatgpt.IChatBot;
import com.xmlin.wechatter.wechatbot.chatgpt.baidu.message.BaiDuAIMessage;
import com.xmlin.wechatter.wechatbot.chatgpt.baidu.message.BaiDuAIMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ChatBot(botSupplier = "baidu")
@Order(1)
@Slf4j
public class BaiDuGPT implements IChatBot
{

    private Pair<String, Date> tokenCache;

    private final Map<String, BaiDuAIMessages> chatCacheMap = new ConcurrentHashMap<>();

    @Value("${wechatter.baiduAIUrl:}")
    private String chatUrl;
    @Value("${wechatter.baiduAIkey:}")
    private String baiduAIkey;
    @Value("${wechatter.baiduAISecret:}")
    private String baiduAISecret;

    public String chat(String input, String user) {
        try {
            String url = chatUrl + "?access_token=%s";
            BaiDuAIMessages baiDuAIMessages = chatCacheMap.computeIfAbsent(user, s -> new BaiDuAIMessages());
            baiDuAIMessages.add(new BaiDuAIMessage("user", input));
            HttpResponse response = HttpUtil.createPost(String.format(url, getToken())).body(baiDuAIMessages.toString())
                    .execute();
            if (!response.isOk()) {
                throw new IOException("Unexpected code " + response);
            }
            JSONObject jsonObject = JSON.parseObject(response.body());
            String result = jsonObject.getString("result");
            baiDuAIMessages.add(new BaiDuAIMessage("assistant", "result"));
            return result;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return "抱歉，聊天服务暂时不可用。";
        }
    }

    @Override
    public void clearChatCacheMap(String user) {
        chatCacheMap.remove(user);
    }

    public String getToken() {
        if (tokenCache == null || tokenCache.getValue().compareTo(new Date()) < 0) {
            String url = "https://aip.baidubce.com/oauth/2.0/token";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("grant_type", "client_credentials");
            paramMap.put("client_id", baiduAIkey);
            paramMap.put("client_secret", baiduAISecret);
            try {
                String tokenInfoString = HttpUtil.get(url, paramMap);
                JSONObject jsonObject = JSON.parseObject(tokenInfoString);
                tokenCache = Pair.of(jsonObject.getString("access_token"), DateUtil.offsetSecond(new Date(),
                        (jsonObject.getInteger("expires_in") - (jsonObject.getInteger("expires_in") / 10000))));
            }
            catch (Exception e) {
                tokenCache = null;
                log.error("获取百度token失败", e);
            }
        }
        return tokenCache.getKey();
    }
}
