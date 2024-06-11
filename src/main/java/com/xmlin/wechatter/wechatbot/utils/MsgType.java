package com.xmlin.wechatter.wechatbot.utils;

public enum MsgType
{
    文字("text"),
    链接卡片("urlLink"),
    图片("file"),
    视频("file"),
    附件("file"),
    语音("file"),
    添加好友邀请("friendship");

    String type;

    MsgType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
