package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别：用于用户资料编辑等前端下拉。
 */
@Getter
@AllArgsConstructor
public enum Gender {

    UNKNOWN(0, "保密"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final Integer code;
    private final String desc;
}
