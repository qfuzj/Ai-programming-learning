package com.campus.resourcesharing.common.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    USER("user"),
    ADMIN("admin");

    private final String code;

    UserRoleEnum(String code) {
        this.code = code;
    }

    public static UserRoleEnum fromCode(String code) {
        for (UserRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
