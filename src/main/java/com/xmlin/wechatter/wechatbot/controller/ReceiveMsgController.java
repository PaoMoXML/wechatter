package com.xmlin.wechatter.wechatbot.controller;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatGPT;
import com.xmlin.wechatter.wechatbot.commands.CommandFactory;
import com.xmlin.wechatter.wechatbot.entity.WeChatterRtn;
import com.xmlin.wechatter.wechatbot.enums.IsOrNot;
import com.xmlin.wechatter.wechatbot.enums.MsgType;
import com.xmlin.wechatter.wechatbot.enums.WeChatterRtnContentType;
import com.xmlin.wechatter.wechatbot.utils.MailFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReceiveMsgController
{

    @Value("${wechatter.commandPrefix}")
    public String commandPrefix;

    /**
     * gpt工具类
     */
    private final ChatGPT chatGPT;

    /**
     * 邮件工厂
     */
    private final MailFactory mailFactory;

    /**
     * webhook 主入口
     *
     * @param type          类型 {@link MsgType MsgType.class}
     * @param content       传输的内容, 文本或传输的文件共用这个字段，结构映射请看示例
     * @param source        消息的相关发送方数据, JSON String
     * @param isMentioned   该消息是@我的消息
     * @param isMsgFromSelf 是否是来自自己的消息
     */
    @RequestMapping("receive_msg")
    public String receiveMsg(@RequestParam String type, @RequestParam Object content, @RequestParam String source,
            @RequestParam String isMentioned, @RequestParam String isMsgFromSelf) {

        // 是自己的请求
        if (IsOrNot.是.getValue().equals(isMsgFromSelf)) {
            return WeChatterRtn.FAIL().buildRtnString();
        }
        MsgType by = EnumUtil.getBy(MsgType.class, msgType -> type.equals(msgType.getType()));

        JSONObject sourceJson = JSON.parseObject(source);

        if (by != null) {
            // 群相关
            if (isGroup(sourceJson)) {
                if (IsOrNot.否.getValue().equals(isMentioned)) {
                    return WeChatterRtn.FAIL().buildRtnString();
                }
                return group(by, sourceJson, content);
            }
            else {
                return personal(by, sourceJson, content);
            }
        }
        else {
            log.warn("获取到的type为：{}，无对应的处理", type);
        }
        return WeChatterRtn.FAIL().buildRtnString();
    }

    private boolean isGroup(JSONObject sourceJson) {
        JSONObject room = sourceJson.getJSONObject("room");
        return !room.isEmpty();
    }

    private String group(MsgType msgType, JSONObject sourceJson, Object content) {
        JSONObject from = sourceJson.getJSONObject("from");
        JSONObject room = sourceJson.getJSONObject("room");
        // 消息发送者姓名
        String fromName = from.getJSONObject("payload").getString("name");
        log.info("群事件，content：{}，source：{}", content, sourceJson);

        return WeChatterRtn.FAIL().buildRtnString();
    }

    private String personal(MsgType msgType, JSONObject sourceJson, Object content) {
        String source = sourceJson.toJSONString();
        switch (msgType) {
            case 文字 -> {
                String contentString = String.valueOf(content);
                JSONObject from = sourceJson.getJSONObject("from");
                // 消息发送者姓名
                String fromName = from.getJSONObject("payload").getString("name");
                log.info("receive---<{}>:<{}>", fromName, content);
                String rtnContent;
                // 执行命令
                if (contentString.startsWith(commandPrefix)) {
                    rtnContent = new CommandFactory(contentString, fromName).doCmd();
                }
                // 执行聊天
                else {
                    rtnContent = chatGPT.myDoChat(contentString, fromName);
                }
                log.info("send---<{}>:<{}>", fromName, rtnContent);
                return WeChatterRtn.OK().setRtnContent(WeChatterRtnContentType.text, rtnContent).buildRtnString();
            }

            case 图片 -> {
                log.info("图片事件，content：{}，source：{}", content, source);
            }
            case 视频 -> log.info("视频事件，content：{}，source：{}", content, source);
            case 语音 -> log.info("语音事件，content：{}，source：{}", content, source);
            case 附件 -> log.info("附件事件，content：{}，source：{}", content, source);
            case 链接卡片 -> log.info("链接卡片事件，content：{}，source：{}", content, source);
            case 添加好友邀请 -> log.info("添加好友邀请事件，content：{}，source：{}", content, source);

            case 登录 -> log.info("登录事件，content：{}，source：{}", content, source);
            case 登出 -> {
                log.info("登出事件，content：{}，source：{}", content, source);
                // 发送登录提醒
                mailFactory.sendLogoutWarnning();
            }
            case 异常报错 -> log.info("异常报错，content：{}，source：{}", content, source);
            case 快捷回复后消息推送状态通知 ->
                    log.info("快捷回复后消息推送状态通知，content：{}，source：{}", content, source);
            // 表情包在这
            case 未实现的消息类型 -> log.info("未实现的消息类型，content：{}，source：{}", content, source);

            default -> log.info("获取到的类型为：{}，暂时没有处理方法", msgType.getType());
        }
        return WeChatterRtn.FAIL().buildRtnString();
    }

}
