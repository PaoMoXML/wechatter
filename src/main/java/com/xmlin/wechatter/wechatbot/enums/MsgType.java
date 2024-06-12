package com.xmlin.wechatter.wechatbot.enums;

public enum MsgType
{
    文字("text"),
    链接卡片("urlLink"),
    图片("file"),
    视频("file"),
    附件("file"),
    语音("file"),
    添加好友邀请("friendship"),

    未实现的消息类型("unknown"),
    登录("system_event_login"),
    登出("system_event_logout"),
    异常报错("system_event_error"),
    快捷回复后消息推送状态通知("system_event_push_notify");

    String type;

    MsgType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
