package com.travel.advisor.exception;

import com.travel.advisor.common.context.RequestContext;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return buildError(ex.getResultCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常，包括 @Valid 和 @Validated 触发的异常，以及其他常见的参数错误异常
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class })
    public Result<Void> handleValidationException(Exception ex) {
        String message = "参数错误";
        if (ex instanceof MethodArgumentNotValidException validEx && validEx.getBindingResult().hasErrors()) {
            FieldError fieldError = validEx.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        if (ex instanceof BindException bindEx && bindEx.getBindingResult().hasErrors()) {
            FieldError fieldError = bindEx.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        return buildError(ResultCode.BAD_REQUEST, message);
    }

    // 处理其他未捕获的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildError(ResultCode.SYSTEM_ERROR, ResultCode.SYSTEM_ERROR.getMessage());
    }

    // 构建错误响应
    private Result<Void> buildError(ResultCode code, String message) {
        Result<Void> result = Result.fail(code, message);
        result.setRequestId(RequestContext.getRequestId());
        return result;
    }
}
