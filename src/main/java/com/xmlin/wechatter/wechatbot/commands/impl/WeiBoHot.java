package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.utils.CommandType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 微博热搜
 */
@Command(type = CommandType.wbhot)
@Slf4j
public class WeiBoHot implements ICommand
{
    private final String url = "https://m.weibo.cn/api/container/getIndex?containerid=106003%26filter_type%3Drealtimehot";

    @Override
    public String apply(String useless) {
        try {
            String hotJson = HttpUtil.get(url, 5000);
            JSONArray jsonArray = JSON.parseObject(hotJson).getJSONObject("data").getJSONArray("cards").getJSONObject(0)
                    .getJSONArray("card_group");
            int loopCount = Math.min(jsonArray.size(), 20);
            List<String> hotStringList = new ArrayList<>();
            for (int i = 0; i < loopCount; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                hotStringList.add((i + 1) + "." + jsonObject.getString("desc"));
            }
            return "✨=====微博热搜=====✨\n" + String.join("\n", hotStringList);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return "获取微博热搜失败！";
        }
    }

    @Override
    public boolean checkArgs(String useless) {
        return true;
    }

}
