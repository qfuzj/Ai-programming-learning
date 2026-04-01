package com.campus.resourcesharing.common.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    CONSULT("consult"),
    SYSTEM("system"),
    ORDER("order");

    private final String code;

    MessageTypeEnum(String code) {
        this.code = code;
    }
}
