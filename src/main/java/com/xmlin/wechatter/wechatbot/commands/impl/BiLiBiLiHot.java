package com.xmlin.wechatter.wechatbot.commands.impl;

import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;

/**
 * b站热搜
 */
@Command(type = CommandType.bilihot)
public class BiLiBiLiHot implements ICommand
{
    @Override
    public boolean checkArgs(String args) {
        return true;
    }

    @Override
    public String apply(String s) {
        return null;
    }
}
