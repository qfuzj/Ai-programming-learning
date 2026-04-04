package com.travel.advisor.config;

import com.travel.advisor.common.context.RequestContext;
import com.travel.advisor.common.result.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一结果包装器
 */
@ControllerAdvice
public class ResponseAdviceConfig implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果返回类型是 String，则不进行包装，直接返回原始字符串
        return !returnType.getParameterType().equals(String.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof Result<?> result) {
            if (result.getRequestId() == null) {
                result.setRequestId(RequestContext.getRequestId());
            }
            if (result.getTimestamp() == null) {
                result.setTimestamp(System.currentTimeMillis());
            }
            return result;
        }
        if (selectedConverterType == StringHttpMessageConverter.class) {
            return body;
        }
        Result<Object> result = Result.success(body);
        result.setRequestId(RequestContext.getRequestId());
        return result;
    }
}
