package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作日志模块枚举
 */
@Getter
@AllArgsConstructor
public enum OperationLogModule {

    SCENIC("scenic", "景点管理"),
    USER("user", "用户管理"),
    REVIEW("review", "评论管理"),
    CONFIG("config", "系统配置"),
    TAG("tag", "标签管理"),
    RECOMMEND("recommend", "推荐系统");

    private final String code;
    private final String desc;

    public static OperationLogModule fromCode(String code) {
        for (OperationLogModule module : OperationLogModule.values()) {
            if (module.code.equals(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("不支持的操作模块: " + code);
    }
}
