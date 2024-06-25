package com.xmlin.wechatter.wechatbot.chatgpt;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ChatExceptionAOP
{

    @Pointcut(value = "execution(* com.xmlin.wechatter.wechatbot.chatgpt..*.chat(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed();
        }
        catch (Throwable e) {
            log.error(e.getMessage(), e);
            return "抱歉，聊天服务暂时不可用。";
        }
    }
}
