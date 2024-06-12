package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.commands.impl.*;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
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
    private final String cmdString;

    private final WeiBoHot weiBoHot = SpringUtil.getBean(WeiBoHot.class);
    private final Weather weather = SpringUtil.getBean(Weather.class);
    private final RandomInt randomInt = SpringUtil.getBean(RandomInt.class);
    private final ClearGTP clearGTP = SpringUtil.getBean(ClearGTP.class);
    private final SendRemind sendRemind = SpringUtil.getBean(SendRemind.class);

    public CommandFactory(String cmdString, String userName) {
        this.cmdString = cmdString;
        this.userName = userName;
        // 提取命令和参数的逻辑提取到一个单独的方法中
        Pair<CommandType, String> commandTypeStringPair = parseCommand(cmdString);
        this.command = commandTypeStringPair.getLeft();
        this.commandArgs = commandTypeStringPair.getRight();
    }

    public CommandFactory(String cmdString) {
        this.cmdString = cmdString;
        this.userName = null;
        // 提取命令和参数的逻辑提取到一个单独的方法中
        Pair<CommandType, String> commandTypeStringPair = parseCommand(cmdString);
        this.command = commandTypeStringPair.getLeft();
        this.commandArgs = commandTypeStringPair.getRight();
    }

    private Pair<CommandType, String> parseCommand(String cmdString) {
        final CommandType command;
        final String commandArgs;

        String commandContent = cmdString.replaceFirst("/", "");
        // 带参数的命令
        if (commandContent.contains(" ")) {
            String[] splitedCommand = commandContent.split(" ", 2);
            if (splitedCommand.length == 2) {
                command = CommandType.getCommandType(splitedCommand[0]);
                commandArgs = splitedCommand[1];
            }
            else {
                command = null;
                commandArgs = null;
            }
        }
        // 普通命令
        else {
            command = CommandType.getCommandType(commandContent);
            commandArgs = null;
        }
        return Pair.of(command, commandArgs);
    }

    /**
     * 检查命令
     *
     * @return
     */
    public boolean checkCommand() {
        if (command == null) {
            throw new IllegalStateException("cmd解析失败：" + cmdString);
        }
        boolean isValid;
        switch (command) {
            case wbhot -> isValid = weiBoHot.checkArgs(null);
            case weather -> isValid = weather.checkArgs(commandArgs);
            //                case bilihot ->
            case randomint -> isValid = randomInt.checkArgs(commandArgs);
            case clearGPT -> isValid = clearGTP.checkArgs(userName);
            // 发送消息不需要做额外处理
            case sendRemind -> isValid = sendRemind.checkArgs(commandArgs);
            default -> isValid = false;
        }

        return isValid;
    }

    public String doCmd() {
        String rtnContent;
        if (checkCommand()) {
            switch (command) {
                case wbhot -> rtnContent = weiBoHot.apply(null);
                case weather -> rtnContent = weather.apply(commandArgs);
                //                case bilihot ->
                case randomint -> rtnContent = randomInt.apply(commandArgs);
                case clearGPT -> rtnContent = clearGTP.apply(userName);
                // 发送消息不需要做额外处理
                case sendRemind -> rtnContent = sendRemind.apply(commandArgs);
                default -> rtnContent = "不支持的命令";
            }
        }
        else {
            log.error("命令有误：{}，执行命令的用户：{}", cmdString,
                    CharSequenceUtil.isBlank(userName) ? "系统cron执行" : userName);
            rtnContent = "不支持的命令";
        }
        return rtnContent;
    }

    public CommandType getCommand() {
        return command;
    }

    public String getCommandArgs() {
        return commandArgs;
    }
}
