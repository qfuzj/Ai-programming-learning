package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 景点状态枚举，对应 scenic_spot.status 字段。
 */
@Getter
@AllArgsConstructor
public enum ScenicSpotStatus {

    INACTIVE(0, "不可见/下架"),
    ACTIVE(1, "正常"),
    PENDING(2, "审核中");

    private final Integer code;
    private final String desc;

    public static ScenicSpotStatus fromCode(Integer code) {
        for (ScenicSpotStatus status : ScenicSpotStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的景点状态代码: " + code);
    }
}
