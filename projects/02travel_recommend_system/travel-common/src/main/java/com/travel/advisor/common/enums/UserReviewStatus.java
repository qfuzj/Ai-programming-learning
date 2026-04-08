package com.travel.advisor.common.enums;

import lombok.Getter;

/**
 * 用户评论审核状态枚举，表示用户评论的审核状态。
 */
@Getter
public enum UserReviewStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核拒绝"),
    HIDDEN(3, "隐藏");

    private final int code;
    private final String desc;

    UserReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
