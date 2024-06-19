package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.RequiredArgsConstructor;

/**
 * 清空chatgpt对话
 */
@Command(type = CommandType.clearGPT)
@RequiredArgsConstructor
public class ClearGTP implements ICommand
{

    private final SwitchChatBot switchChatBot;

    @Override
    public String apply(String userName) {
        if (CharSequenceUtil.isNotBlank(userName)) {
            switchChatBot.getToUsedBot().clearChatCacheMap(userName);
        }
        return "聊天上下文清空成功清空成功！";
    }

    @Override
    public boolean checkArgs(String userName) {
        return CharSequenceUtil.isNotBlank(userName);
    }
}
