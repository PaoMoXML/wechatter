package com.xmlin.wechatter.wechatbot.commands;

import java.util.function.UnaryOperator;

public interface ICommand extends UnaryOperator<String>
{
    boolean checkArgs(String args);
}
