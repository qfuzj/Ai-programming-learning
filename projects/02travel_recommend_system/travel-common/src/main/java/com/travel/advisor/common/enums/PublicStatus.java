package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 前端可见性枚举
 */
@Getter
@AllArgsConstructor
public enum PublicStatus {

    PRIVATE(0, "不可见"),
    PUBLIC(1, "可见");

    private final int code;
    private final String desc;

    public static PublicStatus fromCode(Integer code) {
        for (PublicStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("不支持的可见状态: " + code);
    }
}
