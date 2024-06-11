package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.core.util.RandomUtil;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.extern.slf4j.Slf4j;

import java.util.function.UnaryOperator;

@Command(type = CommandType.randomint)
@Slf4j
public class RandomInt implements UnaryOperator<String>
{

    @Override
    public String apply(String args) {
        int min = 0;
        int max;
        try {
            if (args.contains("-") && args.split("-").length == 2) {
                String[] split = args.split("-");
                min = Integer.parseInt(split[0]);
                max = Integer.parseInt(split[1]);
            }
            else {
                max = Integer.parseInt(args);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return "参数错误";
        }

        return RandomUtil.randomInt(min, max, true, true) + "";
    }

}
