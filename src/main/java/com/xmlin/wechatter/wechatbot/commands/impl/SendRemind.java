package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.extern.slf4j.Slf4j;

@Command(type = CommandType.sendRemind)
@Slf4j
public class SendRemind implements ICommand
{
    @Override
    public boolean checkArgs(String args) {
        return CharSequenceUtil.isNotBlank(args);
    }

    @Override
    public String apply(String remind) {
        return remind;
    }
}
