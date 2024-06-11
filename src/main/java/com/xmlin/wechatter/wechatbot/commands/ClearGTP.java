package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.core.text.CharSequenceUtil;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatGPT;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

/**
 * 清空chatgpt对话
 */
@Command(type = CommandType.clearGPT)
@RequiredArgsConstructor
public class ClearGTP implements UnaryOperator<String>
{
    private final ChatGPT chatGPT;

    @Override
    public String apply(String userName) {
        if (CharSequenceUtil.isNotBlank(userName)) {
            chatGPT.clearChatCacheMap(userName);
        }
        return "聊天上下文清空成功清空成功！";
    }
}
