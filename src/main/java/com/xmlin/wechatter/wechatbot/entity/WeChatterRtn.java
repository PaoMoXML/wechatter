package com.xmlin.wechatter.wechatbot.entity;

import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.enums.WeChatterRtnContentType;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class WeChatterRtn
{
    @AllArgsConstructor
    private static class RtnData
    {

        private WeChatterRtnContentType weChatterRtnContentType;

        private String content;

        public JSONObject toJSON() {
            return new JSONObject().fluentPut("type", weChatterRtnContentType.name()).fluentPut("content", content);
        }
    }

    private final boolean success;

    List<JSONObject> rtnDataList = new ArrayList<>();

    private WeChatterRtn(boolean success) {
        this.success = success;
    }

    public static WeChatterRtn OK() {
        return new WeChatterRtn(true);
    }

    public static WeChatterRtn FAIL() {
        return new WeChatterRtn(false);
    }

    public WeChatterRtn setRtnContent(WeChatterRtnContentType weChatterRtnContentType, String content) {
        rtnDataList.add(new RtnData(weChatterRtnContentType, content).toJSON());
        return this;
    }

    public String buildRtnString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", this.success);
        jsonObject.put("data", this.rtnDataList);
        return jsonObject.toJSONString();
    }
}
