package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

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

    private final String commandPrefix = SpringUtil.getProperty("wechatter.commandPrefix");
    private final Map<CommandType, ICommand> cmdCache = SpringUtil.getBean(CommandCache.class).getCmdCache();

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

        String commandContent = cmdString.replaceFirst(commandPrefix, "");
        // 带参数的命令
        if (commandContent.trim().contains(" ")) {
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
            if (command == CommandType.clearGPT) {
                commandArgs = userName;
            }
            else {
                commandArgs = null;
            }
        }
        return Pair.of(command, commandArgs);
    }

    /**
     * 检查命令
     *
     * @return
     */
    public boolean checkCmd() {
        if (command == null) {
            throw new IllegalStateException("cmd解析失败：" + cmdString);
        }
        return cmdCache.get(command).checkArgs(commandArgs);
    }

    public String doCmd() {
        String rtnContent;
        try {
            if (checkCmd()) {
                rtnContent = cmdCache.get(command).apply(commandArgs);
            }
            else {
                log.error("命令有误：{}，执行命令的用户：{}", cmdString,
                        CharSequenceUtil.isBlank(userName) ? "系统cron执行" : userName);
                rtnContent = "命令有误";
            }
        }
        catch (Exception e) {
            rtnContent = "命令有误";
            log.error(e.getMessage(), e);
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
