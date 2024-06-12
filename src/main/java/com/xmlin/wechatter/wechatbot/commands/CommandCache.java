package com.xmlin.wechatter.wechatbot.commands;

import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@Slf4j
public class CommandCache
{
    private final Map<CommandType, ICommand> cmdCache = new EnumMap<>(CommandType.class);

    /**
     * 使用Spring机制，获取所有的命令类，并映射存储到缓存
     */
    private CommandCache() {
        log.info("--------------开始注册命令--------------");
        Map<String, ICommand> beansOfCommand = SpringUtil.getBeansOfType(ICommand.class);
        for (Map.Entry<String, ICommand> stringICommandEntry : beansOfCommand.entrySet()) {
            ICommand cmd = stringICommandEntry.getValue();
            try {
                MergedAnnotations mergedAnnotations = MergedAnnotations.from(cmd.getClass());
                MergedAnnotation<Command> commandMergedAnnotation = mergedAnnotations.get(Command.class);
                CommandType type = commandMergedAnnotation.getEnum("type", CommandType.class);
                cmdCache.put(type, cmd);
                log.info("成功注册：[{}]", cmd.getClass());
            }
            catch (Exception e) {
                log.error("失败注册：[" + cmd.getClass() + "]", e);
            }

        }
        log.info("--------------结束注册命令--------------");
    }

    public Map<CommandType, ICommand> getCmdCache() {
        return cmdCache;
    }
}
