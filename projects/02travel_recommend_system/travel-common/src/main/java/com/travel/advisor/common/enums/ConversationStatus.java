package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话状态枚举
 */
@Getter
@AllArgsConstructor
public enum ConversationStatus {

    CLOSED(0, "关闭"),
    ACTIVE(1, "进行中");

    private final int code;
    private final String desc;

    public static ConversationStatus fromCode(int code) {
        for (ConversationStatus type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的会话状态: " + code);
    }
}
