package com.github.freenamu.backend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {
    @Pointcut("within(com.github.freenamu.backend.controller..*)")
    public void onRequest() {
    }

    @Around("onRequest()")
    public Object logMethodParametersAndTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> aClass = joinPoint.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(aClass);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            logger.info(getMethodName(joinPoint) + getParameters(joinPoint) + " " + stopWatch.getTotalTimeMillis() + "ms");
        }
    }

    private String getMethodName(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.getName();
    }

    private JSONObject getParameters(JoinPoint joinPoint) throws JSONException {
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        JSONObject params = new JSONObject();
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], args[i]);
        }
        return params;
    }
}
