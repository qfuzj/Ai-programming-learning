package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行政区划层级：对应 region 表 level 字段。
 */
@Getter
@AllArgsConstructor
public enum RegionLevel {

    PROVINCE(1, "省"),
    CITY(2, "市"),
    DISTRICT(3, "区县");

    private final Integer code;
    private final String desc;
}
