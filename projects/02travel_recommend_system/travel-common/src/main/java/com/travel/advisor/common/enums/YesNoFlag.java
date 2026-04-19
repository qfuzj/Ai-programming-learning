package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用是否标志：用于 region.is_hot 等 0/1 字段。
 * 与 {@link PublicStatus} 区分：{@link PublicStatus} 语义是可见性，本枚举是通用是否。
 */
@Getter
@AllArgsConstructor
public enum YesNoFlag {

    NO(0, "否"),
    YES(1, "是");

    private final Integer code;
    private final String desc;
}
