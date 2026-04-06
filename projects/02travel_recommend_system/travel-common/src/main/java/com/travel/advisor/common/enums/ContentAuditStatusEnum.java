package com.travel.advisor.common.enums;

import lombok.Getter;

@Getter
public enum ContentAuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    MANUAL_REVIEW(3, "人工复审");

    private final int code;
    private final String desc;

    ContentAuditStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
