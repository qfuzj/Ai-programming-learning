package com.campus.resourcesharing.common.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String code;

    ReportStatusEnum(String code) {
        this.code = code;
    }
}
