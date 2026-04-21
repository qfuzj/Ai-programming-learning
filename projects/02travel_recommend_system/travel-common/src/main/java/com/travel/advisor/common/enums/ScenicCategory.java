package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 景点分类枚举：对应 scenic_spot.category 字段
 */
@Getter
@AllArgsConstructor
public enum ScenicCategory {

    NATURE("自然风光", "自然风光"),
    CULTURE("人文古迹", "人文古迹"),
    THEME_PARK("主题公园", "主题公园"),
    MUSEUM("博物馆", "博物馆"),
    TEMPLE("宗教寺庙", "宗教寺庙"),
    PARK("城市公园", "城市公园"),
    WATER("水上乐园", "水上乐园"),
    MOUNTAIN("名山胜岳", "名山胜岳"),
    HISTORIC("历史遗迹", "历史遗迹"),
    RESORT("度假休闲", "度假休闲"),
    OTHER("其他", "其他");

    private final String code;
    private final String desc;

    public static ScenicCategory fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        for (ScenicCategory category : values()) {
            if (category.code.equals(code.trim())) {
                return category;
            }
        }
        return OTHER;
    }
}
