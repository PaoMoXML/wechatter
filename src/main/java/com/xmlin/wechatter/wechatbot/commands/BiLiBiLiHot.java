package com.xmlin.wechatter.wechatbot.commands;

import com.xmlin.wechatter.wechatbot.utils.CommandType;

import java.util.function.Supplier;

/**
 * b站热搜
 */
@Command(type = CommandType.bilihot)
public class BiLiBiLiHot implements Supplier<String>
{
    @Override
    public String get() {
        return null;
    }
}
