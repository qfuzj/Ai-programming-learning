package com.travel.advisor.aspect;

import com.travel.advisor.annotation.OperationLog;
import com.travel.advisor.entity.OperationLogE;
import com.travel.advisor.mapper.OperationLogMapper;
import com.travel.advisor.security.LoginUser;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;

/**
 * 操作日志切面，负责拦截标注了@OperationLog注解的方法，记录操作日志并保存到数据库中。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        String errorMessage = null;
        Integer status = 1;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            status = 0;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            saveLog(joinPoint, operationLog, result, status, errorMessage, System.currentTimeMillis() - start);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint,
                         OperationLog operationLog,
                         Object result,
                         Integer status,
                         String errorMessage,
                         long duration) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getRoleType() == null || !"ADMIN".equalsIgnoreCase(loginUser.getRoleType())) {
            return;
        }

        HttpServletRequest request = getRequest();
        OperationLogE log = new OperationLogE();
        log.setAdminUserId(loginUser.getUserId());
        log.setAdminUsername(loginUser.getUsername());
        log.setModule(operationLog.module());
        log.setAction(operationLog.action());
        log.setDescription(operationLog.description());
        log.setRequestMethod(request == null ? null : request.getMethod());
        log.setRequestUrl(request == null ? null : request.getRequestURI());
        log.setRequestParams(JsonUtils.toJson(safeArgs(joinPoint.getArgs())));
        log.setResponseData(result == null ? null : truncate(JsonUtils.toJson(result), 2000));
        log.setIpAddress(request == null ? null : request.getRemoteAddr());
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        log.setExecutionTimeMs((int) duration);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        operationLogMapper.insert(log);
    }

    private Object[] safeArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(Objects::nonNull)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .filter(arg -> !(arg instanceof HttpServletResponse))
                .toArray();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
