package com.george.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Pointcut("execution(* com.george.service.*.*(..))")
    public void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        
        // Generate request ID for tracking
        String requestId = UUID.randomUUID().toString();
        
        // Log method entry with sanitized arguments
        logger.info("[{}] Entering {}.{}() with args: {}", 
            requestId, className, methodName, sanitizeArguments(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log method exit with execution time
            if (result != null && result.toString().length() > 200) {
                logger.info("[{}] Exiting {}.{}() - execution time: {}ms - result: [truncated]", 
                    requestId, className, methodName, executionTime);
            } else {
                logger.info("[{}] Exiting {}.{}() - execution time: {}ms - result: {}", 
                    requestId, className, methodName, executionTime, result);
            }
            
            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[{}] Exception in {}.{}() after {}ms", 
                requestId, className, methodName, executionTime, e);
            throw e;
        }
    }

    private String sanitizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) {
                    return "null";
                }
                String argStr = arg.toString();
                // Truncate long strings (like user profiles)
                if (argStr.length() > 100) {
                    return argStr.substring(0, 100) + "... [truncated]";
                }
                // Mask sensitive data patterns
                if (argStr.contains("token") || argStr.contains("password") || argStr.contains("secret")) {
                    return "[REDACTED]";
                }
                return argStr;
            })
            .reduce((a, b) -> a + ", " + b)
            .orElse("[]");
    }
}

