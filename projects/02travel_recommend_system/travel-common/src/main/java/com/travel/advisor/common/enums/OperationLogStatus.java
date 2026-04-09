package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作日志状态枚举
 */
@Getter
@AllArgsConstructor
public enum OperationLogStatus {
    SUCCESS(1, "成功"),
    FAIL(0, "失败");

    private final int code;
    private final String desc;

    public static OperationLogStatus fromCode(int code) {
        for (OperationLogStatus status : OperationLogStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("不支持的操作状态: " + code);
    }
}

