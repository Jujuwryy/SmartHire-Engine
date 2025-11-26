package com.george.aspect;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorHandlingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingAspect.class);

    @Pointcut("execution(* com.george.service.*.*(..))")
    public void serviceMethods() {}

    @Pointcut("execution(* com.george.controller.*.*(..))")
    public void controllerMethods() {}

    @AfterThrowing(pointcut = "serviceMethods() || controllerMethods()", throwing = "exception")
    public void logServiceException(Exception exception) {
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        
        if (exception instanceof IllegalArgumentException || 
            exception instanceof IllegalStateException) {
            logger.warn("Business logic exception: {} - {}", exceptionType, message);
        } else {
            logger.error("Unexpected exception: {} - {}", exceptionType, message, exception);
        }
    }
}

