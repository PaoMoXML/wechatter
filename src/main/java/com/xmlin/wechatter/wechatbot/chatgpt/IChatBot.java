package com.xmlin.wechatter.wechatbot.chatgpt;

public interface IChatBot
{

    String chat(String input, String user);

    void clearChatCacheMap(String user);
}
