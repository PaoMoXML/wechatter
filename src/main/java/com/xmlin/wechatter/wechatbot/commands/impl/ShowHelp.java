package com.xmlin.wechatter.wechatbot.commands.impl;

import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
        List<String> msgs = new ArrayList<>();
        for (CommandType commandType : commandTypes) {
            msgs.add(helpMsg(commandType));
        }
        return String.join("-----------\n", msgs);
    }

    public static String helpMsg(CommandType commandType) {
        return String.format("☀️命令：%s %n🌙别名：%s %n✨描述：%s %n", commandType.name(), commandType.getAlias(),
                commandType.getDescription());
    }
}
