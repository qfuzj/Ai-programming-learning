package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件资源上传者类型
 */
@Getter
@AllArgsConstructor
public enum FileResourceUploaderType {
    /**
     * 上传者类型：1 用户 2 管理员 3 系统
     */
    USER(1, "用户"),
    ADMIN(2, "管理员"),
    SYSTEM(3, "系统");

    private final Integer code;
    private final String desc;

    public static FileResourceUploaderType fromCode(Integer code) {
        for (FileResourceUploaderType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的上传者类型代码: " + code);
    }

    public static FileResourceUploaderType fromRoleType(String roleType) {
        if ("ADMIN".equalsIgnoreCase(roleType)) {
            return ADMIN;
        } else if ("USER".equalsIgnoreCase(roleType)) {
            return USER;
        } else {
            return SYSTEM;
        }
    }
}
