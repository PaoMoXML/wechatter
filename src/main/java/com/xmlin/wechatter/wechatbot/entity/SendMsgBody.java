package com.xmlin.wechatter.wechatbot.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendMsgBody
{

    String to;
    String content;

    public JSONObject build() {
        return new JSONObject().fluentPut("to", to).fluentPut("data", new JSONObject().fluentPut("content", content));
    }

}
