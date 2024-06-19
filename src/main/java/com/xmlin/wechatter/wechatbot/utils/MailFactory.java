package com.xmlin.wechatter.wechatbot.utils;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "wechatter")
@Slf4j
@Data
public class MailFactory
{

    private Map<String, String> mail;
    MailAccount account;

    @Value("${wechatter.webhookUrl}")
    public String webhookUrl;
    @Value("${wechatter.webhookToken}")
    public String token;

    public MailFactory() {
        account = new MailAccount();
    }

    public void sendLogoutWarnning() {
        account.setHost(String.valueOf(mail.get("host")));
        account.setPort(Integer.parseInt(mail.get("port") + ""));
        account.setAuth(true);
        account.setFrom(String.valueOf(mail.get("from")));
        account.setUser(String.valueOf(mail.get("user")));
        account.setPass(String.valueOf(mail.get("pass")));
        account.setSslEnable(Boolean.parseBoolean(mail.get("sslenbale") + ""));

        try {
            String src = null;
            String toUseUrl = webhookUrl + "/login?token=" + token;
            Document document = Jsoup.connect(toUseUrl).get();
            Elements iframe = document.getElementsByTag("iframe");
            for (Element element : iframe) {
                src = element.attr("src");
            }
            MailUtil.send(account, mail.get("to") + "", "微信登出",
                    "<a href=" + src + "> " + "已经退出了:" + src + "</a>", true);
            log.info("登出提醒已发送");
        }
        catch (Exception e) {
            log.info("登出提醒发送失败", e);
        }
    }

}
