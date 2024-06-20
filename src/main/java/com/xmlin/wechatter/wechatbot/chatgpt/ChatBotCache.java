package com.xmlin.wechatter.wechatbot.chatgpt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class ChatBotCache
{

    private final Map<String, IChatBot> botSupplierCacheMap = new TreeMap<>();

    private final List<IChatBot> chatBotList = new ArrayList<>();

    ChatBotCache() {
        log.info("--------------开始注册AI--------------");
        Map<String, IChatBot> chatBotMap = SpringUtil.getBeansOfType(IChatBot.class);
        for (Map.Entry<String, IChatBot> stringIChatBotEntry : chatBotMap.entrySet()) {
            IChatBot chatBot = stringIChatBotEntry.getValue();
            // 在类上搜索注解
            MergedAnnotations mergedAnnotations = MergedAnnotations.from(chatBot.getClass());
            MergedAnnotation<ChatBot> commandMergedAnnotation = mergedAnnotations.get(ChatBot.class);
            // 获取注解的botSupplier属性值
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
        // 根据@order排序
        AnnotationAwareOrderComparator.sort(chatBotList);
        log.info("--------------结束注册AI--------------");
    }

    public Map<String, IChatBot> getBotSupplierCacheMap() {
        return botSupplierCacheMap;
    }

    public List<IChatBot> getChatBotList() {
        return chatBotList;
    }
}
