package com.travel.advisor.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 敏感状态枚举，表示内容是否敏感。
 */
@Getter
@AllArgsConstructor
public enum SensitiveStatus {
    NOT_SENSITIVE(0, "非敏感"),
    SENSITIVE(1, "敏感");

    @JsonValue  // Jackson 序列化时使用 code（可选）
    private final Integer code;
    private final String desc;

    public static SensitiveStatus fromCode(Integer code) {
        for (SensitiveStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的敏感状态代码: " + code);
    }
}