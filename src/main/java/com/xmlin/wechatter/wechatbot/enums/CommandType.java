package com.xmlin.wechatter.wechatbot.enums;

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
    weather("天气,天气预报"),
    /**
     * 随机数
     */
    randomint("随机数,随机,roll"),
    /**
     * 清空ChatGPT对话
     */
    clearGPT("clearGPT,clear,清空,清空对话,新建对话"),
    /**
     * 发送提醒
     */
    sendRemind("sendRemind"),
    /**
     * 切换聊天机器人
     */
    switchChatBot("switch,切换,切换聊天机器人");

    private String alias;

    public static CommandType getCommandType(String commandContent) {
        return EnumUtil.getBy(CommandType.class,
                commandType -> commandType.name().equals(commandContent) || Arrays.asList(
                        commandType.getAlias().split(",")).contains(commandContent));
    }
}
