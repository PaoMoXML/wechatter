package com.xmlin.wechatter.wechatbot.controller;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xmlin.wechatter.wechatbot.chatgpt.ChatGPT;
import com.xmlin.wechatter.wechatbot.commands.CommandFactory;
import com.xmlin.wechatter.wechatbot.entity.WeChatterRtn;
import com.xmlin.wechatter.wechatbot.utils.IsOrNot;
import com.xmlin.wechatter.wechatbot.utils.MailFactory;
import com.xmlin.wechatter.wechatbot.utils.MsgType;
import com.xmlin.wechatter.wechatbot.utils.WeChatterRtnContentType;
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
    public String receiveMsg(@RequestParam String type, @RequestParam String content, @RequestParam String source,
            @RequestParam String isMentioned, @RequestParam String isMsgFromSelf) {

        // 是自己的请求
        if (IsOrNot.是.getValue().equals(isMsgFromSelf)) {
            return WeChatterRtn.FAIL().buildRtnString();
        }
        MsgType by = EnumUtil.getBy(MsgType.class, msgType -> type.equals(msgType.getType()));

        JSONObject sourceJson = JSON.parseObject(source);

        if (by != null) {
            switch (by) {
                case 文字 -> {
                    JSONObject from = sourceJson.getJSONObject("from");
                    // 消息发送者姓名
                    String fromName = from.getJSONObject("payload").getString("name");
                    log.info("receive---<{}>:<{}>", fromName, content);
                    String rtnContent;
                    // 执行命令
                    if (content.startsWith(commandPrefix)) {
                        rtnContent = new CommandFactory(content, fromName).doCmd();
                    }
                    // 执行聊天
                    else {
                        //                        rtnContent = chatGPT.doChat(content, fromName);
                        rtnContent = chatGPT.myDoChat(content, fromName);
                    }
                    String rtn = WeChatterRtn.OK().setRtnContent(WeChatterRtnContentType.text, rtnContent)
                            .buildRtnString();
                    log.info("send---<{}>:<{}>", fromName, rtnContent);
                    return rtn;
                }

                case 图片 -> log.info("图片事件，content：{}，source：{}", content, source);
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

                default -> log.info("获取到的类型为：{}，暂时没有处理方法", by.getType());
            }
        }
        else {
            log.warn("获取到的type为：{}，无对应的处理", type);
        }
        return WeChatterRtn.FAIL().buildRtnString();
    }

}
