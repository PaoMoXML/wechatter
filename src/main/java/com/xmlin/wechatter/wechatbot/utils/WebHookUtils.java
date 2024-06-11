package com.xmlin.wechatter.wechatbot.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpUtil;
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

    /**
     * 发送微信消息
     *
     * @param to      发送人
     * @param content 发送内容
     */
    public void sendMsg(String to, String content) {
        Assert.notBlank(to, "发送人不能为空");
        log.info("send---<{}>:<{}>", to, content);
        HttpUtil.post(webhookUrl, new SendMsgBody(to, content).build().toJSONString());
    }

}
