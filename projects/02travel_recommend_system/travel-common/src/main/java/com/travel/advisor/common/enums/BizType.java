package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum BizType {

    REVIEW(1, "点评"),
    AVATAR(2, "头像"),
    PLAN(3, "行程计划"),
    CHAT(4, "AI对话");

    private final Integer code;
    private final String desc;

    public static BizType fromCode(Integer code) {
        for (BizType type : BizType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的业务类型代码: " + code);
    }
}
