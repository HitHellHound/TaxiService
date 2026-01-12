package com.efcon.tg_notification.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class IgnoreExceptionAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreExceptionAspect.class);

    @Around("@within(ignoreExceptions)")
    public Object logAndIgnoreDriverChatNotFoundException(ProceedingJoinPoint pjp, IgnoreExceptions ignoreExceptions) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception exception) {
            boolean isExceptionToIgnore = Arrays.stream(ignoreExceptions.exceptions())
                    .anyMatch(exceptionType -> exceptionType.isAssignableFrom(exception.getClass()));
            if (isExceptionToIgnore) {
                LOGGER.info("Ignore exception {} with message: {}", exception.getClass(), exception.getMessage());
                return null;
            }
            throw exception;
        }
    }
}
