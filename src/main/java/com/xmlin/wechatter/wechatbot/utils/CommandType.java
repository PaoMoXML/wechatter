package com.xmlin.wechatter.wechatbot.utils;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CommandType
{
    /**
     * 微博热搜
     */
    wbhot("微博热搜,微博"),
    /**
     * b站热搜
     */
    bilihot("b站热搜,b站"),
    /**
     * 天气
     */
    weather("天气,天气预报");

    private String alias;

    public static CommandType getCommandType(String commandContent) {
        return EnumUtil.getBy(CommandType.class,
                commandType -> commandType.name().equals(commandContent) || Arrays.asList(
                        commandType.getAlias().split(",")).contains(commandContent));
    }
}
