package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.utils.CommandType;

public class CommandFactory
{

    /**
     * 命令类型
     */
    private final CommandType command;
    /**
     * 命令参数
     */
    private final String commandArgs;
    private final String userName;

    private final WeiBoHot weiBoHot = SpringUtil.getBean(WeiBoHot.class);
    private final Weather weather = SpringUtil.getBean(Weather.class);
    private final RandomInt randomInt = SpringUtil.getBean(RandomInt.class);
    private final ClearGTP clearGTP = SpringUtil.getBean(ClearGTP.class);

    public CommandFactory(String cmdString, String userName) {
        String commandContent = cmdString.replaceFirst("/", "");
        // 带参数的命令
        if (commandContent.contains(" ")) {
            String[] splitedCommand = commandContent.split(" ");
            if (splitedCommand.length == 2) {
                this.command = CommandType.getCommandType(splitedCommand[0]);
                this.commandArgs = splitedCommand[1];
            }
            else {
                this.command = null;
                this.commandArgs = null;
            }
        }
        // 普通命令
        else {
            this.command = CommandType.getCommandType(commandContent);
            this.commandArgs = null;
        }
        this.userName = userName;
    }

    public String doCmd() {
        String rtnContent;
        if (command != null) {
            switch (command) {
                case wbhot -> rtnContent = weiBoHot.get();
                case weather -> rtnContent = weather.apply(commandArgs);
                //                case bilihot ->
                case randomint -> rtnContent = randomInt.apply(commandArgs);
                case clearGPT -> rtnContent = clearGTP.apply(userName);
                default -> rtnContent = "不支持的命令";
            }
        }
        else {
            rtnContent = "不支持的命令";
        }
        return rtnContent;
    }
}
