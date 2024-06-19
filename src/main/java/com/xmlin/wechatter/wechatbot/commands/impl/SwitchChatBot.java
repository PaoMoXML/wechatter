package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatBot;
import com.xmlin.wechatter.wechatbot.chatgpt.IChatBot;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Command(type = CommandType.switchChatBot)
@Slf4j
public class SwitchChatBot implements ICommand
{

    private final Map<String, IChatBot> botSupplierCacheMap = new TreeMap<>();

    private final List<IChatBot> chatBotList = new ArrayList<>();

    private final LoopingListIterator<IChatBot> loopingListIterator;

    private IChatBot toUsedBot;

    SwitchChatBot() {
        log.info("--------------开始注册AI--------------");
        Map<String, IChatBot> chatBotMap = SpringUtil.getBeansOfType(IChatBot.class);
        for (Map.Entry<String, IChatBot> stringIChatBotEntry : chatBotMap.entrySet()) {
            IChatBot chatBot = stringIChatBotEntry.getValue();
            MergedAnnotations mergedAnnotations = MergedAnnotations.from(chatBot.getClass());
            MergedAnnotation<ChatBot> commandMergedAnnotation = mergedAnnotations.get(ChatBot.class);
            String botSupplier = commandMergedAnnotation.getString("botSupplier");
            botSupplierCacheMap.compute(botSupplier, (s, iChatBot) -> {
                if (iChatBot != null) {
                    throw new IllegalStateException("botSupplier：" + botSupplier + "已经被注册过了");
                }
                return chatBot;
            });
            log.info("成功注册：[{}]", chatBot.getClass());
            chatBotList.add(chatBot);
        }
        if (CollUtil.isEmpty(chatBotList)) {
            throw new IllegalStateException("没有一个聊天机器人被注册！");
        }
        AnnotationAwareOrderComparator.sort(chatBotList);
        this.toUsedBot = chatBotList.get(0);
        log.info("--------------结束注册AI--------------");
        loopingListIterator = new LoopingListIterator<>(chatBotList);
    }

    @Override
    public boolean checkArgs(String args) {
        return true;
    }

    @Override
    public String apply(String botSupplier) {
        if (CharSequenceUtil.isBlank(botSupplier) || botSupplierCacheMap.get(botSupplier) == null) {
            IChatBot next = loopingListIterator.next();
            MergedAnnotations mergedAnnotations = MergedAnnotations.from(next.getClass());
            MergedAnnotation<ChatBot> commandMergedAnnotation = mergedAnnotations.get(ChatBot.class);
            String defBotSupplier = commandMergedAnnotation.getString("botSupplier");
            toUsedBot = next;
            return "切换至供应商：" + defBotSupplier;
        }
        toUsedBot = botSupplierCacheMap.get(botSupplier);
        return "切换至供应商：" + botSupplier;
    }

    public IChatBot getToUsedBot() {
        return toUsedBot;
    }

}
