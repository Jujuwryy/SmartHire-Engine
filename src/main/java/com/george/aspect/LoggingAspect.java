package com.george.aspect;

import com.george.util.Constants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
        
        try {
            logger.debug("Entering {}.{}() with args: {}", 
                className, methodName, sanitizeArguments(args));
            
            long startTime = System.currentTimeMillis();
            
            try {
                Object result = joinPoint.proceed();
                long executionTime = System.currentTimeMillis() - startTime;
                
                if (result != null) {
                    String resultStr = result.toString();
                    if (resultStr.length() > Constants.MAX_RESULT_DISPLAY_LENGTH) {
                        logger.debug("Exiting {}.{}() - execution time: {}ms - result: [truncated]", 
                            className, methodName, executionTime);
                    } else {
                        logger.debug("Exiting {}.{}() - execution time: {}ms - result: {}", 
                            className, methodName, executionTime, result);
                    }
                } else {
                    logger.debug("Exiting {}.{}() - execution time: {}ms - result: null", 
                        className, methodName, executionTime);
                }
                
                if (executionTime > Constants.SLOW_OPERATION_THRESHOLD_MS) {
                    logger.warn("Slow operation detected: {}.{}() took {}ms", 
                        className, methodName, executionTime);
                }
                
                return result;
            } catch (Throwable e) {
                long executionTime = System.currentTimeMillis() - startTime;
                logger.error("Exception in {}.{}() after {}ms - error: {}", 
                    className, methodName, executionTime, e.getMessage(), e);
                throw e;
            }
        } catch (Throwable e) {
            logger.error("Error in logging aspect", e);
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
                if (argStr.length() > Constants.MAX_ARGUMENT_DISPLAY_LENGTH) {
                    return argStr.substring(0, Constants.MAX_ARGUMENT_DISPLAY_LENGTH) + "... [truncated]";
                }
                if (argStr.contains("token") || argStr.contains("password") || argStr.contains("secret")) {
                    return "[REDACTED]";
                }
                return argStr;
            })
            .reduce((a, b) -> a + ", " + b)
            .orElse("[]");
    }
}

