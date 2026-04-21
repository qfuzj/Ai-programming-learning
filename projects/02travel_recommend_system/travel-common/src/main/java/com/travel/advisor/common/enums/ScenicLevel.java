package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 景点等级枚举：对应 scenic_spot.level 字段
 */
@Getter
@AllArgsConstructor
public enum ScenicLevel {

    LEVEL_5A("5A", "5A级景区"),
    LEVEL_4A("4A", "4A级景区"),
    LEVEL_3A("3A", "3A级景区"),
    LEVEL_2A("2A", "2A级景区"),
    LEVEL_1A("1A", "1A级景区"),
    NONE("", "未评级");

    private final String code;
    private final String desc;

    public static ScenicLevel fromCode(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        for (ScenicLevel level : values()) {
            if (level.code.equalsIgnoreCase(code.trim())) {
                return level;
            }
        }
        return NONE;
    }
}
