package com.campus.resourcesharing.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    TRADING("trading"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    CLOSED("closed");

    private final String code;

    OrderStatusEnum(String code) {
        this.code = code;
    }
}
