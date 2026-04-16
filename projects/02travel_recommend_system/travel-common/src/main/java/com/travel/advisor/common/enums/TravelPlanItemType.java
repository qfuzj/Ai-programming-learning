package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行程项类型枚举
 */
@Getter
@AllArgsConstructor
public enum TravelPlanItemType {

    SCENIC(1, "景点"),
    FOOD(2, "餐饮"),
    HOTEL(3, "住宿"),
    TRANSPORT(4, "交通"),
    CUSTOM(5, "自定义");

    private final Integer code;
    private final String desc;

    public static TravelPlanItemType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TravelPlanItemType type : TravelPlanItemType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}