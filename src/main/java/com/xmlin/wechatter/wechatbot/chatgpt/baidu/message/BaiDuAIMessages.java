package com.xmlin.wechatter.wechatbot.chatgpt.baidu.message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.LinkedList;

public class BaiDuAIMessages
{

    private final LinkedList<BaiDuAIMessage> messageList = new LinkedList<>();

    public synchronized void add(BaiDuAIMessage baiDuAIMessage) {
        if (messageList.size() > 30) {
            messageList.removeFirst();
        }
        messageList.add(baiDuAIMessage);
    }

    @Override
    public String toString() {
        JSONArray array = new JSONArray();
        array.addAll(messageList);
        return new JSONObject().fluentPut("messages", array).toJSONString();
    }
}
