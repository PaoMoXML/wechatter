package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.commands.impl.ShowHelp;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
        this.command = commandTypeStringPair.getKey();
        this.commandArgs = commandTypeStringPair.getValue();
    }

    public CommandFactory(String cmdString) {
        this.cmdString = cmdString;
        this.userName = null;
        // 提取命令和参数的逻辑提取到一个单独的方法中
        Pair<CommandType, String> commandTypeStringPair = parseCommand(cmdString);
        this.command = commandTypeStringPair.getKey();
        this.commandArgs = commandTypeStringPair.getValue();
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
                rtnContent = tryToCorrect("命令格式错误", cmdString);
            }
        }
        catch (Exception e) {
            rtnContent = tryToCorrect("没有找到命令", cmdString);
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

    /**
     * 尝试纠错命令，并返回相关示例
     *
     * @param errmsg
     * @param cmdString
     * @return
     */
    public static String tryToCorrect(String errmsg, String cmdString) {
        EnumSet<CommandType> commandTypes = EnumSet.allOf(CommandType.class);
        List<Pair<CommandType, Double>> pairList = new ArrayList<>();
        for (CommandType commandType : commandTypes) {
            // 相似程度
            double similar = StrUtil.similar(cmdString, commandType.name());
            // 别名相似度
            for (String alias : commandType.getAlias().split(",")) {
                double similarAlias = StrUtil.similar(cmdString, alias);
                if (similarAlias > similar) {
                    similar = similarAlias;
                }
            }
            if (similar > 0) {
                pairList.add(Pair.of(commandType, similar));
            }
        }

        // 排序
        pairList.sort((o1, o2) -> {
            if (o1.getValue().equals(o2.getValue())) {
                return 0;
            }
            else if (o1.getValue() > o2.getValue()) {
                return -1;
            }
            else {
                return 1;
            }
        });
        if (pairList.size() > 3) {
            pairList = pairList.subList(0, 3);
        }
        List<String> correctMsg = new ArrayList<>();
        for (Pair<CommandType, Double> pair : pairList) {
            correctMsg.add(ShowHelp.helpMsg(pair.getKey()));
        }
        if (CollUtil.isEmpty(correctMsg)) {
            return "";
        }
        return String.format("%s%n最相似的命令是：%n%s", errmsg, String.join("-----------\n", correctMsg));
    }
}
