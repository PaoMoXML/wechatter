package com.xmlin.wechatter.wechatbot.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.entity.SendMsgBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebHookUtils
{

    @Value("${wechatter.webhookUrl}")
    public String webhookUrl;
    @Value("${wechatter.webhookToken}")
    public String token;

    /**
     * 发送微信消息
     *
     * @param to      发送人
     * @param content 发送内容
     */
    public void sendMsg(String to, String content) {
        Assert.notBlank(to, "发送人不能为空");
        String toUseUrl = webhookUrl + "/webhook/msg/v2?token=" + token;
        String post = HttpUtil.post(toUseUrl, new SendMsgBody(to, content).build().toJSONString());
        JSONObject jsonObject = JSON.parseObject(post);
        if (!jsonObject.getBooleanValue("success")) {
            log.warn("消息发送失败：{} <{}>:<{}>", jsonObject.getString("message"), to, content);
        }
        else {
            log.info("send---<{}>:<{}>", to, content);
        }
    }

}
