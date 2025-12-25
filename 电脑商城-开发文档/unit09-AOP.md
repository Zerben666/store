# 统计业务方法耗时

     在**不改变项目主体流程代码**的前提条件下完成检测项目所有的业务层方法的耗时（开始执行时间和结束执行之差）的功能。

## AOP

面向切面编程。它并不是Spring框架的特性，Spring很好的支持AOP编程。

如果我们想对业务某一些方法同时添加相同的功能需求，并且在不改变原有的业务功能逻辑的基础上进行完成，可以使用AOP的切面编程进行开发。

> 1.先定义一个类，将这个类作为切面类。
2.在这个类中定义切面方法(5类)。
3.将这个切面方法中的业务逻辑要执行的代码进行编写和设计。
4.通过连接点来连接目标方法，就是用粗粒度表达式和细粒度表达式进行连接。

## 切面方法

1.切面方法修饰符必须是public。
2.切面方法的返回值可以是void和Object。如果这个方法被@Around注解修饰，此方法必须声明为Object类型，反之随意。
3.切面方法的方法名称可以自定义。
4.切面方法可以接收参数，参数是ProccedingJoinPoint接口类型的参数。但是@Aroud所修饰方法必须要传递这个参数，其他随意。

<br/>

### 统计业务方法执行时长

1.AOP不是Springl的内部封装的技术，所以使用需要进行导包操作。

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjtools</artifactId>
</dependency>
```

2.定义一个切面类aop.TimerAspect

```java
package com.cy.store.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component    // 将当前类的对象创建使用维护交由Spring容器
@Aspect       // 将当前类标记为切面类
public class TimerAspect {
  
}
```

3.使用环绕通知的方式定义切面方法。ProceedingJoinPoint接口表示连接点（目标方法的对象）

```java
// 使用环绕通知的方式定义切面方法
    public Object around(ProceedingJoinPoint pjp) {
        // 先记录当前时间
        long start = System.currentTimeMillis();
        Object result = pjp.proceed(); // 调用目标方法：login
        // 后记录当前时间
        long end = System.currentTimeMillis();
        System.out.println( "耗时：" + (end-start) );

        return  result;
    }
```

4.将当前环绕通知映射到某个切面上（指定连接点）

```
@Around("execution(* com.cy.store.service.impl.*.*(..))")
```

5.启动项目，随机去访问任意功能模块。

<br/>

==FINISH,完结撒花，感谢陪伴==
