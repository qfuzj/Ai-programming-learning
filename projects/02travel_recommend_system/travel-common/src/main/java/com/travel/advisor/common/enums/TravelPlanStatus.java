package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行程计划发布状态：对应 travel_plan.status 字段。
 */
@Getter
@AllArgsConstructor
public enum TravelPlanStatus {

    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布");

    private final Integer code;
    private final String desc;
}
