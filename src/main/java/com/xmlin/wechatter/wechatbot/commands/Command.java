package com.xmlin.wechatter.wechatbot.commands;

import com.xmlin.wechatter.wechatbot.utils.CommandType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Command
{
    CommandType type();

}
