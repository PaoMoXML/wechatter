package com.xmlin.wechatter.wechatbot.cron;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.SpringUtil;
import com.xmlin.wechatter.wechatbot.commands.CommandFactory;
import com.xmlin.wechatter.wechatbot.utils.WebHookUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "crontasklist")
@Slf4j
@Data
public class CronJobFactory implements CommandLineRunner
{

    private List<Map<String, Object>> tasks;

    public void parseCronTasks() {
        for (Map<String, Object> task : tasks) {

            boolean enable = Convert.toBool(task.get("enable")) != null && Convert.toBool(task.get("enable"));

            String taskname = String.valueOf(task.get("taskname"));
            //检查是否启用
            if (!enable) {
                continue;
            }
            else {
                log.info("开始注册任务：{}", taskname);
            }
            String cron = String.valueOf(task.get("cron"));
            String timezone = String.valueOf(task.get("timezone"));

            List<String> cmds = new ArrayList<>();
            List<List<String>> toPersonLists = new ArrayList<>();

            Object commands = task.get("commands");
            if (commands instanceof LinkedHashMap<?, ?> mapcCommands) {
                for (Object value : mapcCommands.values()) {
                    if (value instanceof LinkedHashMap<?, ?> mapcCommand) {
                        try {
                            // 命令
                            String cmd = String.valueOf(mapcCommand.get("cmd"));
                            // 入参
                            LinkedHashMap<?, ?> args = (LinkedHashMap<?, ?>) mapcCommand.get("args");
                            List<String> argsList = args.values().stream().map(String::valueOf).toList();
                            // 发送给谁
                            LinkedHashMap<?, ?> topersonlist = (LinkedHashMap<?, ?>) mapcCommand.get("topersonlist");
                            List<String> toPersonList = topersonlist.values().stream().map(String::valueOf).toList();
                            doCmd(cron, cmd, argsList, toPersonList);
                            cmds.add(cmd);
                            toPersonLists.add(toPersonList);
                        }
                        catch (Exception e) {
                            log.warn("任务：" + taskname + "中的子命令：" + value + "配置失败，跳过", e);
                        }
                    }
                }

            }
            log.info("注册任务成功：{} ，cron表达式为：{}，cmds:{} ,toPersonLists:{}", taskname, cron, cmds, toPersonLists);
        }

        // 支持秒级别定时任务
        TimeZone shanghai = TimeZone.getTimeZone("Asia/Shanghai");
        CronUtil.getScheduler().setTimeZone(shanghai);
        CronUtil.setMatchSecond(true);
        CronUtil.start();

    }

    public void doCmd(String cron, String cmd, List<String> argsList, List<String> toPersonList) {

        WebHookUtils webHookUtils = SpringUtil.getBean(WebHookUtils.class);

        CronUtil.schedule(cron, (Task) () -> {
            log.info("执行cmd：{}", cmd);
            CommandFactory commandFactory;
            if (CollectionUtil.isNotEmpty(argsList)) {
                commandFactory = new CommandFactory(cmd + " " + String.join(" ", argsList));
            }
            else {
                commandFactory = new CommandFactory(cmd);
            }
            if (CollectionUtil.isNotEmpty(toPersonList)) {
                for (String toUser : toPersonList) {
                    webHookUtils.sendMsg(toUser, commandFactory.doCmd());
                }
            }
            else {
                log.warn("命令：{}，发送人为空", cmd);
            }
        });
    }

    @Override
    public void run(String... args) throws Exception {
        parseCronTasks();
    }

}
