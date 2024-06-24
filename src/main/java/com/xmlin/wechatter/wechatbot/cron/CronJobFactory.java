package com.xmlin.wechatter.wechatbot.cron;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
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
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private boolean keeplife;

    private static int ONE_MINUTE = 1000 * 60;

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
                            // 执行检查
                            CommandFactory commandFactory = checkJobConfig(cron, cmd, argsList, toPersonList);
                            // 添加任务
                            addCmdToCronJob(cron, commandFactory, toPersonList);
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

    private CommandFactory checkJobConfig(String cron, String cmd, List<String> argsList, List<String> toPersonList) {
        // 检查发送人
        if (CollUtil.isEmpty(toPersonList)) {
            throw new IllegalStateException(CharSequenceUtil.format("cmd：{}，发送人为空", cmd));
        }

        // 检查命令
        CommandFactory commandFactory;
        // 有参
        if (CollUtil.isNotEmpty(argsList)) {
            commandFactory = new CommandFactory(cmd + " " + String.join(" ", argsList));
        }
        // 无参
        else {
            commandFactory = new CommandFactory(cmd);
        }
        if (!commandFactory.checkCmd()) {
            throw new IllegalStateException(CharSequenceUtil.format("命令有误，cmd：{}，args：{}", cmd, argsList));
        }

        // 输入cron测试
        if (CronExpression.isValidExpression(cron)) {
            CronExpression parse = CronExpression.parse(cron);
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
            List<String> nextFive = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                LocalDateTime next = parse.next(now);
                if (next != null) {
                    now = next;
                    nextFive.add(DateUtil.format(next, DatePattern.NORM_DATETIME_PATTERN));
                }
            }
            log.info("cmd：{}->{} 下五次执行时间为：{}", cmd, toPersonList, nextFive);
        }
        else {
            throw new IllegalStateException(CharSequenceUtil.format("cron表达式有误，cron：{}", cron));
        }
        return commandFactory;

    }

    /**
     * 将命令添加到cron任务
     *
     * @param cron           cron表达式
     * @param commandFactory 命令工厂
     * @param toPersonList   接收人
     */
    public void addCmdToCronJob(String cron, CommandFactory commandFactory, List<String> toPersonList) {
        WebHookUtils webHookUtils = SpringUtil.getBean(WebHookUtils.class);
        CronUtil.schedule(cron, (Task) () -> {
//            sleepRandomDelaySeconds(cron);
            log.info("执行cmd：{}，args：{}，toPersons：{}", commandFactory.getCommand().name(),
                    commandFactory.getCommandArgs(), toPersonList);
            for (String toUser : toPersonList) {
                webHookUtils.sendMsg(toUser, commandFactory.doCmd());
            }
        });
    }

    //    private Map<String, Integer> durationNanoCache = new HashMap<>();

    private void sleepRandomDelaySeconds(String cron) {
        //        int second = durationNanoCache.computeIfAbsent(cron, s -> {
        //            LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
        //            LocalDateTime next = CronExpression.parse(s).next(now);
        //            LocalDateTime next2 = CronExpression.parse(s).next(next);
        //            Duration between = Duration.between(next, next2);
        //            return Convert.toInt(between.getSeconds() / 10);
        //        });
        // 可能会让掉线减少？
        try {
            Thread.sleep(RandomUtil.randomInt(0, 5000));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) {
        if (keeplife) {
            keepLife();
        }
        parseCronTasks();
    }

    /**
     * 每一个小时发送一条消息给 文件传输助手 不知能否让掉线减少
     */
    private void keepLife() {
        WebHookUtils webHookUtils = SpringUtil.getBean(WebHookUtils.class);
        CronUtil.schedule("0 0 */1 * * *", (Task) () -> {
            try {
                Thread.sleep(RandomUtil.randomInt(ONE_MINUTE, ONE_MINUTE * 30));
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("执行存活保持");
            webHookUtils.sendMsg("文件传输助手", new CommandFactory("weather 张家港").doCmd());
        });
        webHookUtils.sendMsg("文件传输助手", new CommandFactory("weather 张家港").doCmd());
    }

}
