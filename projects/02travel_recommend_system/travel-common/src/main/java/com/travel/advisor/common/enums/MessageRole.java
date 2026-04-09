package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色枚举，表示消息的发送者角色。
 */
@Getter
@AllArgsConstructor
public enum MessageRole {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String role;
}
