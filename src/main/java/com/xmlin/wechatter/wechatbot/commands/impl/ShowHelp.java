package com.xmlin.wechatter.wechatbot.commands.impl;

import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;

import java.util.EnumSet;

@Command(type = CommandType.help)
public class ShowHelp implements ICommand
{

    @Override
    public boolean checkArgs(String args) {
        return true;
    }

    @Override
    public String apply(String s) {
        EnumSet<CommandType> commandTypes = EnumSet.allOf(CommandType.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (CommandType commandType : commandTypes) {
            stringBuilder.append(String.format("命令：%s 别名：%s 描述：%s %n", commandType.name(), commandType.getAlias(),
                    commandType.getDescription()));
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        System.err.println(new ShowHelp().apply(""));
    }
}
