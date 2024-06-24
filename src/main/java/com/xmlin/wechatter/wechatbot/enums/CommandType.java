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
    wbhot("微博热搜,微博", "发送微博前二十个热搜 使用示例:/微博"),
    /**
     * b站热搜
     */
    bilihot("b站热搜,b站", "发送b站热搜 使用示例:/b站"),
    /**
     * 天气
     */
    weather("天气,天气预报", "发送指定地区热搜 使用示例:/天气 苏州"),
    /**
     * 随机数
     */
    randomint("随机数,随机,roll", "发送指定范围随机数 使用示例:/roll 1-100"),
    /**
     * 清空ChatGPT对话
     */
    clearGPT("clearGPT,clear,清空,清空对话,新建对话", "将与GPT的对话记忆清空 使用示例:/clear"),
    /**
     * 发送提醒
     */
    sendRemind("sendRemind", "将发送的内容返回 使用示例:/sendRemind 你好"),
    /**
     * 切换聊天机器人
     */
    switchChatBot("switch,切换,切换聊天机器人", "切换聊天机器人 使用示例:/切换 或 /切换 [选项] -> /切换 baidu"),
    /**
     * 帮助
     */
    help("help", "显示所有指令 使用示例:/help");

    private final String alias;

    private final String description;

    public static CommandType getCommandType(String commandContent) {
        return EnumUtil.getBy(CommandType.class,
                commandType -> commandType.name().equals(commandContent) || Arrays.asList(
                        commandType.getAlias().split(",")).contains(commandContent));
    }
}
