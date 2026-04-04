package com.travel.advisor.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private String requestId;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(ResultCode.SUCCESS.getCode())
            .message(ResultCode.SUCCESS.getMessage())
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(resultCode.getMessage())
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
