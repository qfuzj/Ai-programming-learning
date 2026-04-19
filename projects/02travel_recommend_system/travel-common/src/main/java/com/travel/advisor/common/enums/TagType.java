package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签类型：对应 tag 表 type 字段。
 */
@Getter
@AllArgsConstructor
public enum TagType {

    SCENIC(1, "景点标签"),
    PREFERENCE(2, "偏好标签");

    private final Integer code;
    private final String desc;
}
