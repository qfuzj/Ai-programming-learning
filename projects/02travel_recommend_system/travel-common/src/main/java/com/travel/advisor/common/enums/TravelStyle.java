package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 旅行风格：作为 AI 行程生成 DTO 中 {@code travelStyle} 字段的取值来源。
 * DTO 层由 {@code @Pattern} 正则做校验，此枚举作为权威描述 + 后续可扩展入口。
 */
@Getter
@AllArgsConstructor
public enum TravelStyle {

    CLASSIC("classic", "经典打卡"),
    SLOW("slow", "深度慢游"),
    FOOD("food", "美食优先"),
    OUTDOOR("outdoor", "自然户外");

    private final String code;
    private final String desc;

    /** DTO {@code @Pattern} 正则保持与本枚举同步：{@value} */
    public static final String CODE_PATTERN = "^(classic|slow|food|outdoor)?$";

    public static boolean isValidCode(String code) {
        if (code == null || code.isBlank()) {
            return true;
        }
        for (TravelStyle style : values()) {
            if (style.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
