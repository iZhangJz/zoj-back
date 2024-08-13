package com.zjz.zoj.aop;

import com.zjz.zoj.annotation.CodeBoxLog;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 代码沙箱日志切面
 */
@Aspect
@Component
@Slf4j
public class CodeBoxLogAspect {

    @Around("@annotation(codeBoxLog)")
    public Object AroundMethod(ProceedingJoinPoint joinPoint,CodeBoxLog codeBoxLog) throws Throwable{
        log.info("执行方法：{}",joinPoint.getSignature());
        Object proceed = joinPoint.proceed();
        if (proceed != null){
            if (proceed instanceof ExecuteResponse){
                ExecuteResponse response = (ExecuteResponse) proceed;
                log.info("执行结果：{}", response);
            }
        } else {
            log.error("异常：{ proceed is null}");
        }
        return proceed;
    }
}
