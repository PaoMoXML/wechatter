package com.xmlin.wechatter.wechatbot.chatgpt.openai;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatBot;
import com.xmlin.wechatter.wechatbot.chatgpt.IChatBot;
import lombok.extern.slf4j.Slf4j;
import org.devlive.sdk.openai.OpenAiClient;
import org.devlive.sdk.openai.choice.ChatChoice;
import org.devlive.sdk.openai.entity.ChatEntity;
import org.devlive.sdk.openai.entity.MessageEntity;
import org.devlive.sdk.openai.model.CompletionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ChatBot(botSupplier = "openai")
@Order(2)
@Slf4j
public class ChatGPT implements IChatBot
{

    /**
     * gpt用的接口和token
     */
    @Value("${wechatter.openAIUrl}")
    private String openAIUrl;
    @Value("${wechatter.openAIToken}")
    private String openAIToken;
    @Value("${wechatter.openAIMaxTokens:1024}")
    private int openAIMaxTokens;
    @Value("${wechatter.openAITemperature:0.95}")
    private Double openAITemperature;
    @Value("${wechatter.openAITopP:0.75}")
    private Double openAITopP;
    @Value("${wechatter.openAIModel:gpt-3.5-turbo}")
    private String openAIModel;

    /**
     * 和ChatGPT聊天
     *
     * @param inputContent 输入内容
     * @param userName     用户
     * @return
     */
    public String doChat(String inputContent, String userName) {

        // https://api.chatanywhere.tech/v1/chat/completions
        Assert.notBlank(inputContent, "输入内容不能为空");
        Assert.notBlank(userName, "用户不能为空");

        HttpRequest post = HttpUtil.createPost(openAIUrl);

        post.header("Content-Type", "application/json");
        post.bearerAuth(openAIToken);

        String toUse = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"" + inputContent
                + "\"}],\"temperature\":0.7,\"user\":\"" + userName + "\"}";

        post.body(toUse);

        try (HttpResponse response = post.execute()) {
            if (!response.isOk()) {
                throw new IOException("Unexpected code " + response);
            }
            JSONObject rtnJson = JSON.parseObject(response.body());
            JSONArray choices = rtnJson.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            return "抱歉，聊天服务暂时不可用。";
        }

    }

    private final Map<String, LinkedList<MessageEntity>> chatCacheMap = new ConcurrentHashMap<>();

    ThreadLocal<Integer> retryCount = new ThreadLocal<>();

    /**
     * 聊天
     *
     * @param inputContent
     * @param user
     * @return
     */
    @Override
    public String chat(String inputContent, String user) {
        retryCount.remove();
        return doMyChat(inputContent, user);
    }

    private String doMyChat(String inputContent, String user) {
        Integer retry = retryCount.get();
        if (retry == null) {
            retryCount.set(0);
            retry = 0;
        }
        String rtn = null;
        do {
            try (OpenAiClient client = OpenAiClient.builder().apiHost(openAIUrl).apiKey(openAIToken).build()) {
                // 历史消息
                LinkedList<MessageEntity> historyMessages = chatCacheMap.computeIfAbsent(user, s -> new LinkedList<>());
                // 用户发送消息
                addMessage(historyMessages, MessageEntity.builder().content(inputContent).name(user).build());

                CompletionModel model = EnumUtil.getBy(CompletionModel.class,
                        completionModel -> completionModel.getName().equalsIgnoreCase(openAIModel));

                ChatEntity configure = ChatEntity.builder().model(model).maxTokens(openAIMaxTokens)
                        .temperature(openAITemperature).topP(openAITopP).messages(historyMessages).build();
                // 发送聊天
                List<ChatChoice> choices = client.createChatCompletion(configure).getChoices();
                // 将返回值记录进聊天历史
                choices.forEach(choice -> addMessage(historyMessages, choice.getMessage()));

                rtn = choices.stream().map(chatChoice -> chatChoice.getMessage().getContent())
                        .collect(Collectors.joining("\n"));
                break;
            }
            catch (Exception e) {
                // 对于可重试的异常，且重试次数未超过限制，继续重试
                if (retry < 3) {
                    retry++;
                    try {
                        Thread.sleep((long) Math.pow(2, retry) * 100); // 指数退避策略
                    }
                    catch (InterruptedException ex) {
                        log.error(ex.getMessage(), ex);
                        Thread.currentThread().interrupt();
                    }
                }
                else {
                    throw e;
                }
            }
        }
        while (retry < 3);
        return rtn;
    }

    /**
     * 控制list不超过30
     *
     * @param messages
     * @param message
     */
    private static synchronized void addMessage(LinkedList<MessageEntity> messages, MessageEntity message) {
        if (messages.size() > 30) {
            messages.removeFirst();
        }
        messages.add(message);
    }

    public void clearChatCacheMap(String userName) {
        chatCacheMap.remove(userName);
    }

}
