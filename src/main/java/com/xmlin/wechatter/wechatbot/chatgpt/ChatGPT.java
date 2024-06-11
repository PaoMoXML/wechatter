package com.xmlin.wechatter.wechatbot.chatgpt;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.devlive.sdk.openai.OpenAiClient;
import org.devlive.sdk.openai.choice.ChatChoice;
import org.devlive.sdk.openai.entity.ChatEntity;
import org.devlive.sdk.openai.entity.MessageEntity;
import org.devlive.sdk.openai.model.CompletionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChatGPT
{

    /**
     * gpt用的接口和token
     */
    @Value("${wechatter.openAPIUrl}")
    public String openAPIUrl;
    @Value("${wechatter.openAPItoken}")
    public String openAPItoken;

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

        HttpRequest post = HttpUtil.createPost(openAPIUrl);

        post.header("Content-Type", "application/json");
        post.bearerAuth(openAPItoken);

        String toUse =
                "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"assistant\",\"content\":\"" + inputContent
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

    private final Map<String, List<MessageEntity>> chatCacheMap = new HashMap<>();

    /**
     * 聊天
     *
     * @param inputContent
     * @param user
     * @return
     */
    public String myDoChat(String inputContent, String user) {
        try (OpenAiClient client = OpenAiClient.builder().apiHost(openAPIUrl).apiKey(openAPItoken).build()) {
            List<MessageEntity> messages = chatCacheMap.computeIfAbsent(user, s -> new ArrayList<>());
            messages.add(MessageEntity.builder().content(inputContent).name(user).build());
            ChatEntity configure = ChatEntity.builder().model(CompletionModel.GPT_35_TURBO).messages(messages).build();
            List<ChatChoice> choices = client.createChatCompletion(configure).getChoices();
            choices.forEach(choice -> messages.add(choice.getMessage()));
            return choices.stream().map(chatChoice -> chatChoice.getMessage().getContent())
                    .collect(Collectors.joining("\n"));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return "抱歉，聊天服务暂时不可用。";
        }
    }

    public void clearChatCacheMap(String userName) {
        chatCacheMap.remove(userName);
    }

}
