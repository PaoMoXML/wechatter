package com.xmlin.wechatter.wechatbot.chatgpt.baidu.message;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class BaiDuAIMessage
{
    private String role;
    private String content;

    @Override
    public String toString() {
        return new JSONObject().fluentPut("role", this.role).fluentPut("content", this.content).toJSONString();
    }
}
