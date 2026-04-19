package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出游同伴类型：作为 AI 行程生成 DTO 中 {@code companionType} 字段的取值来源。
 * DTO 层由 {@code @Pattern} 正则做校验，此枚举作为权威描述 + 后续可扩展入口。
 */
@Getter
@AllArgsConstructor
public enum TravelCompanionType {

    SOLO("solo", "独自出行"),
    COUPLE("couple", "情侣"),
    FAMILY("family", "家庭"),
    FRIENDS("friends", "朋友");

    private final String code;
    private final String desc;

    /** DTO {@code @Pattern} 正则保持与本枚举同步：{@value} */
    public static final String CODE_PATTERN = "^(solo|couple|family|friends)?$";

    public static boolean isValidCode(String code) {
        if (code == null || code.isBlank()) {
            return true;
        }
        for (TravelCompanionType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
