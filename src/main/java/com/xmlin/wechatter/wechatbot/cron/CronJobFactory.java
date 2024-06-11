package com.xmlin.wechatter.wechatbot.cron;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
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

    /**
     * crontasklist.tasks
     */
    private List<Map<String, Object>> tasks;
    /**
     * crontasklist.timezone
     */
    private String timezone;

    /**
     * 根据tasks解析任务，并添加到cron任务
     */
    private void parseCronTasks() {
        for (Map<String, Object> task : tasks) {
            // 是否启用
            boolean enable = Convert.toBool(task.get("enable")) != null && Convert.toBool(task.get("enable"));
            String taskname = String.valueOf(task.get("taskname"));
            //检查是否启用
            if (!enable) {
                continue;
            }
            else {
                log.info("开始注册任务：{}", taskname);
            }
            // cron表达式
            String cron = String.valueOf(task.get("cron"));
            StringBuilder logs = new StringBuilder();
            // 命令
            Object commands = task.get("commands");
            if (commands instanceof LinkedHashMap<?, ?> mapcCommands) {
                for (Object value : mapcCommands.values()) {
                    if (value instanceof LinkedHashMap<?, ?> mapcCommand) {
                        try {
                            // 命令
                            String cmd = String.valueOf(mapcCommand.get("cmd"));
                            // 入参
                            List<String> argsList = parseYAMLList("args", mapcCommand);
                            // 接收人
                            List<String> toPersonList = parseYAMLList("topersonlist", mapcCommand);
                            // 添加任务
                            addCmdToCronJob(cron, cmd, argsList, toPersonList);
                            logs.append(cmd).append("->").append(toPersonList).append(" ");
                        }
                        catch (Exception e) {
                            log.warn("任务：[" + taskname + "]中的子命令：[" + value + "]配置失败，跳过", e);
                        }
                    }
                }

                log.info("\n注册任务成功：{} \ncron：{} \ncmds：{}", taskname, cron, logs);
            }
        }

        // cron任务启动和相关配置
        if (CharSequenceUtil.isBlank(timezone)) {
            timezone = "Asia/Shanghai";
        }
        TimeZone shanghai = TimeZone.getTimeZone(timezone);
        CronUtil.getScheduler().setTimeZone(shanghai);
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 解析YAML文件中的list
     *
     * @param key
     * @param mapcCommand
     * @return
     */
    private List<String> parseYAMLList(String key, LinkedHashMap<?, ?> mapcCommand) {
        Object objList = mapcCommand.get(key);
        List<String> argsList;
        if (objList instanceof LinkedHashMap<?, ?> args) {
            argsList = args.values().stream().map(String::valueOf).toList();
        }
        else if (objList instanceof ArrayList<?> args) {
            argsList = (List<String>) args;
        }
        else {
            throw new IllegalStateException("yamllist解析失败，key：" + key + "objlist：" + objList);
        }

        return argsList;
    }

    /**
     * 将命令添加到cron任务
     *
     * @param cron         cron表达式
     * @param cmd          命令
     * @param argsList     入参
     * @param toPersonList 接收人
     */
    public void addCmdToCronJob(String cron, String cmd, List<String> argsList, List<String> toPersonList) {
        WebHookUtils webHookUtils = SpringUtil.getBean(WebHookUtils.class);
        CronUtil.schedule(cron, (Task) () -> {
            log.info("执行cmd：{}", cmd);
            CommandFactory commandFactory;
            // 有参
            if (CollUtil.isNotEmpty(argsList)) {
                commandFactory = new CommandFactory(cmd + " " + String.join(" ", argsList), null);
            }
            // 无参
            else {
                commandFactory = new CommandFactory(cmd, null);
            }
            if (CollUtil.isNotEmpty(toPersonList)) {
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
    public void run(String... args) {
        parseCronTasks();
    }

}
