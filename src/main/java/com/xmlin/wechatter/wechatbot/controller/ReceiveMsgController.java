package com.xmlin.wechatter.wechatbot.controller;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatGPT;
import com.xmlin.wechatter.wechatbot.commands.CommandFactory;
import com.xmlin.wechatter.wechatbot.utils.IsOrNot;
import com.xmlin.wechatter.wechatbot.utils.MsgType;
import com.xmlin.wechatter.wechatbot.utils.WebHookUtils;
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

    private final WebHookUtils webHookUtils;

    /**
     * @param type          类型 {@link MsgType MsgType.class}
     * @param content       传输的内容, 文本或传输的文件共用这个字段，结构映射请看示例
     * @param source        消息的相关发送方数据, JSON String
     * @param isMentioned   该消息是@我的消息
     * @param isMsgFromSelf 是否是来自自己的消息
     */
    @RequestMapping("receive_msg")
    public void receiveMsg(@RequestParam String type, @RequestParam String content, @RequestParam String source,
            @RequestParam String isMentioned, @RequestParam String isMsgFromSelf) {

        // 是自己的请求
        if (IsOrNot.是.getValue().equals(isMsgFromSelf)) {
            return;
        }
        MsgType by = EnumUtil.getBy(MsgType.class, msgType -> type.equals(msgType.getType()));

        JSONObject sourceJson = JSON.parseObject(source);
        JSONObject from = sourceJson.getJSONObject("from");
        // 消息发送者姓名
        String fromName = from.getJSONObject("payload").getString("name");
        log.info("receive---<{}>:<{}>", fromName, content);
        if (by != null) {
            switch (by) {
                case 文字 -> {
                    String rtnContent;
                    // 执行命令
                    if (content.startsWith(commandPrefix)) {
                        rtnContent = new CommandFactory(content).doCmd();
                    }
                    // 执行聊天
                    else {
                        rtnContent = chatGPT.doChat(content, fromName);
                    }
                    webHookUtils.sendMsg(fromName, rtnContent);
                }
                //                case 图片 -> {
                //                }
                //                case 视频 -> {
                //                }
                //                case 语音 -> {
                //                }
                //                case 附件 -> {
                //                }
                //                case 链接卡片 -> {
                //                }
                //                case 添加好友邀请 -> {
                //                }
                default -> log.info("获取到的类型为：{}，暂时没有处理方法", by.getType());
            }
        }
        else {
            log.warn("获取到的type为：{}，无对应的处理", type);
        }
    }

}
