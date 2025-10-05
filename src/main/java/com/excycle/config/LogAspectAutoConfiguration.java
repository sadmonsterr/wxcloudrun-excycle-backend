package com.excycle.config;

import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * global log config
 * @author thor
 */
@Aspect
@Component
public class LogAspectAutoConfiguration {

    @Value("${spring.application.name:unknown-application}")
    String applicationName;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {}

    @Around("springBeanPointcut()")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String controllerClassName = pjp.getTarget().getClass().getName();
        String controllerMethodName = pjp.getSignature().getName();
        // 获取被拦截的方法
        String methodName = request.getMethod();
        // 获取被拦截的方法
        String url = request.getRequestURI();
        String logUrl = methodName + " " + url;
        Object[] args = pjp.getArgs();
        Object[] arguments = parseRequestArguments(args);
        LoggerDelegate log = new LoggerDelegate(controllerClassName, controllerMethodName, logUrl, arguments);
        try {
            log.logRequest();
            Object result = pjp.proceed();
            log.logResponse(result);
            return result;
        } catch (Throwable e) {
            log.logError(e);
            throw e;
        }
    }

    private Object[] parseRequestArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[] {};
        }
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }
        return arguments;
    }

    private static class LoggerDelegate {

        private static final String LINE_NUMBER_KEY = "lineNumber";

        private static final Map<String, org.slf4j.Logger> LOGGER_MAP = new ConcurrentHashMap<>();

        private final Logger logger;

        private final long startTimeMills;

        private final String methodName;

        private final String url;

        private final Object[] arguments;

        private LoggerDelegate(String className, String methodName, String url, Object[] arguments) {
            this.logger = getLog(className);
            this.methodName = methodName;
            this.url = url;
            this.arguments = arguments;
            this.startTimeMills = System.currentTimeMillis();
        }

        private Logger getLog(String className) {
            return LOGGER_MAP.computeIfAbsent(className, LoggerFactory::getLogger);
        }

        public void logRequest() {
            if (isDebugEnabled()) {
                String requestJson;
                try {
                    requestJson = JSONUtil.toJsonStr(arguments);
                } catch (Exception e) {
                    requestJson = Arrays.toString(arguments);
                }
                requestJson = StringUtils.length(requestJson) > 1000 ? StringUtils.substring(requestJson, 0, 999) : requestJson;
                debug("{} request received <--- {}, params: {}", methodName, url, requestJson);
            } else {
                info("{} request received <--- {}.", methodName, url);
            }
        }

        public void logResponse(Object result) {
            long processDurationInMills = System.currentTimeMillis() - startTimeMills;
            if (isDebugEnabled()) {
                String resultString = result == null ? StringUtils.EMPTY : JSONUtil.toJsonStr(result);
                debug("{} send response ---> {} took: {}ms return: {} ", methodName, url, processDurationInMills, resultString);
            } else {
                info("{} send response ---> {} took: {}ms", methodName, url, processDurationInMills);
            }
        }

        public void logError(Throwable e) {
            error("[{}] failed, cause {}, took： {} ms", url, e.getMessage(), System.currentTimeMillis() - startTimeMills, e);
        }

        private void info(String format, Object... arguments) {
            logWithLineNumber("?", logger, Level.INFO, format, arguments);
        }

        private void debug(String format, Object... arguments) {
            logWithLineNumber("?", logger, Level.DEBUG, format, arguments);
        }

        private void error(String format, Object... arguments) {
            logWithLineNumber("?", logger, Level.ERROR, format, arguments);
        }

        private boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        public void logWithLineNumber(String lineNumber, Logger log, Level level, String format, Object... arguments) {
            MDC.put(LINE_NUMBER_KEY, lineNumber);
            if (level == Level.DEBUG) {
                logger.debug(format, arguments);
            } else if (level == Level.INFO) {
                logger.info(format, arguments);
            } else if (level == Level.ERROR) {
                logger.error(format, arguments);
            }
            MDC.remove(LINE_NUMBER_KEY);
        }
    }

}
