package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签作用域：对应 tag 表 scope 字段。
 */
@Getter
@AllArgsConstructor
public enum TagScope {

    SCENIC("SCENIC", "景点标签"),
    PREFERENCE("PREFERENCE", "偏好标签"),
    BOTH("BOTH", "通用标签");

    private final String code;
    private final String desc;

    public static TagScope fromCode(String code) {
        if (code == null) return null;
        for (TagScope s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        throw new IllegalArgumentException("不支持的标签作用域: " + code);
    }
}
