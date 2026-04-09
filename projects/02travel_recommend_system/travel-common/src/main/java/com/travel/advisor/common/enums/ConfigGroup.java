package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统配置分组枚举
 */
@Getter
@AllArgsConstructor
public enum ConfigGroup {
    DEFAULT("default", "默认分组"),
    BASIC("basic", "基础配置"),
    LLM("llm", "AI大模型配置"),
    RECOMMEND("recommend", "推荐系统配置"),
    MINIO("minio", "文件存储配置");

    private final String code;
    private final String desc;

    public static ConfigGroup fromCode(String code) {
        for (ConfigGroup group : values()) {
            if (group.code.equals(code)) return group;
        }
        throw new IllegalArgumentException("不支持的配置分组: " + code);
    }
}
