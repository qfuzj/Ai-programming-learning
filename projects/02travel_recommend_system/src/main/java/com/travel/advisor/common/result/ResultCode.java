package com.travel.advisor.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    UNPROCESSABLE_ENTITY(422, "业务校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    SYSTEM_ERROR(500, "系统异常"),
    LLM_CALL_FAILED(502, "LLM服务调用失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
