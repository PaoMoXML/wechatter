package com.xmlin.wechatter.wechatbot.chatgpt;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ChatBot
{
    String botSupplier();

}
