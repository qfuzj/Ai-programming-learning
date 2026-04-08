package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息内容类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageContentType {

    TEXT(1, "文本"),
    IMAGE(2, "图片");

    private final int code;
    private final String desc;

    public static MessageContentType fromCode(Integer code) {
        for (MessageContentType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("不支持的内容类型: " + code);
    }
}
