package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatBot;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatBotCache;
import com.xmlin.wechatter.wechatbot.chatgpt.IChatBot;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.util.List;
import java.util.Map;

@Command(type = CommandType.switchChatBot)
@Slf4j
public class SwitchChatBot implements ICommand
{

    private final Map<String, IChatBot> botSupplierCacheMap;

    private final LoopingListIterator<IChatBot> loopingListIterator;

    private IChatBot toUsedBot;

    SwitchChatBot(ChatBotCache chatBotCache) {
        this.botSupplierCacheMap = chatBotCache.getBotSupplierCacheMap();
        List<IChatBot> chatBotList = chatBotCache.getChatBotList();
        this.toUsedBot = chatBotList.get(0);
        loopingListIterator = new LoopingListIterator<>(chatBotList);
    }

    @Override
    public boolean checkArgs(String args) {
        return true;
    }

    @Override
    public String apply(String botSupplier) {
        if (CharSequenceUtil.isBlank(botSupplier) || botSupplierCacheMap.get(botSupplier) == null) {
            IChatBot currentBot;
            String botSupplierName;
            // 保证切换肯定是切换成不一样的供应商
            do {
                IChatBot next = loopingListIterator.next();
                MergedAnnotations mergedAnnotations = MergedAnnotations.from(next.getClass());
                MergedAnnotation<ChatBot> commandMergedAnnotation = mergedAnnotations.get(ChatBot.class);
                botSupplierName = commandMergedAnnotation.getString("botSupplier");
                currentBot = next;
            }
            while (currentBot == toUsedBot && loopingListIterator.size() > 1);

            toUsedBot = currentBot;
            return "切换至供应商：" + botSupplierName;
        }
        toUsedBot = botSupplierCacheMap.get(botSupplier);
        return "切换至供应商：" + botSupplier;
    }

    public IChatBot getToUsedBot() {
        return toUsedBot;
    }

}
