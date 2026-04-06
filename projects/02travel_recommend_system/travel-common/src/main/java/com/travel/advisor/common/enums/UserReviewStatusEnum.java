package com.travel.advisor.common.enums;

import lombok.Getter;

@Getter
public enum UserReviewStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    HIDDEN(3, "隐藏");

    private final int code;
    private final String desc;

    UserReviewStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
