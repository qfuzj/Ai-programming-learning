package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置值类型
 */
@Getter
@AllArgsConstructor
public enum ConfigType {
    STRING("string", "字符串"),
    NUMBER("number", "数字"),
    BOOLEAN("boolean", "布尔值"),
    JSON("json", "JSON对象");

    private final String code;
    private final String desc;

    public static ConfigType fromCode(String code) {
        for (ConfigType type : values()) {
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("不支持的配置类型: " + code);
    }
}
