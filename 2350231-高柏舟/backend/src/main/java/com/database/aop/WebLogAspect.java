package com.database.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class WebLogAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // 假设您的 Controller 都在 com.database.controller 包下
    @Pointcut("execution(public * com.database.controller..*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        // 记录请求信息
        log.info("============== API Request START ==============");
        log.info("URL          : {}", request.getRequestURL().toString());
        log.info("HTTP Method  : {}", request.getMethod());
        log.info("IP           : {}", request.getRemoteAddr());
        log.info("Class Method : {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());

        // 记录方法参数（注意：如果参数是敏感信息或大文件，需要省略）
        // 对于 @RequestBody 类型的参数，这里可能只会显示对象引用，而不是实际内容
        log.info("Arguments    : {}", Arrays.toString(joinPoint.getArgs()));
        log.info("=============================================");
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，记录返回内容
        log.info("API Response : {}", ret);
        log.info("=============== API Request END ===============\n");
    }

}