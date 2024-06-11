package com.xmlin.wechatter.wechatbot.chatgpt;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        Assert.notBlank(inputContent, "输入内容不能为空");
        Assert.notBlank(userName, "用户不能为空");

        HttpRequest post = HttpUtil.createPost(openAPIUrl);

        post.header("Content-Type", "application/json");
        post.bearerAuth(openAPItoken);

        String toUse = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"" + inputContent
                + "\"}],\"temperature\":0.7,\"user\":\"" + userName + "\"}";

        post.body(toUse);

        try {
            HttpResponse response = post.execute();
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

}
