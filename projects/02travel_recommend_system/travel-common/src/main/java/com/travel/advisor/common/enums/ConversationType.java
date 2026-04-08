package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话类型枚举
 */
@Getter
@AllArgsConstructor
public enum ConversationType {
    CUSTOMER_SERVICE(1, "customer_service", "智能客服"),
    ITINERARY_PLANNING(2, "itinerary_planning", "行程规划"),
    SCENIC_SPOT_INQUIRY(3, "scenic_spot_inquiry", "景点咨询");

    private final int code;
    private final String scene;
    private final String desc;

    public static ConversationType fromCode(Integer code) {
        for (ConversationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的会话类型代码: " + code);
    }
}
