package com.cy.store.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component    // 将当前类的对象创建使用维护交由Spring容器
@Aspect       // 将当前类标记为切面类
public class TimerAspect {
    @Around("execution(* com.cy.store.service.impl.*.*(..))")
    // 使用环绕通知的方式定义切面方法
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 先记录当前时间
        long start = System.currentTimeMillis();
        Object result = pjp.proceed(); // 调用目标方法：login

        //TODO

        // 后记录当前时间
        long end = System.currentTimeMillis();
        System.out.println( "耗时：" + (end-start) );

        return  result;
    }
}
