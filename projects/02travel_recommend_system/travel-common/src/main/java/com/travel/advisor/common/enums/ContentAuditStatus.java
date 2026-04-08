package com.travel.advisor.common.enums;

import lombok.Getter;

/**
 * 内容审核状态枚举，表示内容在审核流程中的不同状态。
 */
@Getter
public enum ContentAuditStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    MANUAL_REVIEW(3, "人工复审");

    private final int code;
    private final String desc;

    ContentAuditStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
