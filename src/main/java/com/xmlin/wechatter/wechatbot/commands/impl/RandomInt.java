package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.util.RandomUtil;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Command(type = CommandType.randomint)
@Slf4j
public class RandomInt implements ICommand
{

    @Override
    public String apply(String args) {
        int min;
        int max;
        try {
            Pair<Integer, Integer> integerIntegerPair = parseNumber(args);
            min = integerIntegerPair.getLeft();
            max = integerIntegerPair.getRight();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return "参数错误";
        }

        return RandomUtil.randomInt(min, max, true, true) + "";
    }

    @Override
    public boolean checkArgs(String args) {
        try {
            parseNumber(args);
            return true;
        }
        catch (Exception e) {
            return false;
        }

    }

    private static Pair<Integer, Integer> parseNumber(String args) {
        int min = 0;
        int max;
        if (args.contains("-") && args.split("-").length == 2) {
            String[] split = args.split("-");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }
        else {
            max = Integer.parseInt(args);
        }
        return Pair.of(min, max);
    }
}
