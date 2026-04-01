package com.campus.resourcesharing.common.enums;

import lombok.Getter;

@Getter
public enum GoodsStatusEnum {
    PENDING("pending"),
    ON_SALE("on_sale"),
    SOLD("sold"),
    OFF_SHELF("off_shelf"),
    REJECTED("rejected");

    private final String code;

    GoodsStatusEnum(String code) {
        this.code = code;
    }
}
