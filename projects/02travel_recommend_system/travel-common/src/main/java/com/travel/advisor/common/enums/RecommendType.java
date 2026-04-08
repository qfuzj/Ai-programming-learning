package com.travel.advisor.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推荐类型枚举，定义系统中不同的推荐场景。
 */
@Getter
@AllArgsConstructor
public enum RecommendType {

    /**
     * 首页推荐：基于用户画像和历史行为生成的个性化推荐
     */
    HOME(1, "首页"),

    /**
     * 景点相似推荐：基于当前浏览景点的特征推荐相似景点
     */
    SIMILAR(2, "相似"),

    /**
     * 行程推荐：基于行程上下文的推荐
     */
    TRIP(3, "行程"),

    /**
     * 搜索推荐：基于搜索关键词和搜索历史的推荐
     */
    SEARCH(4, "搜索");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取对应的枚举值。
     *
     * @param code 推荐类型代码
     * @return 对应的 RecommendType 枚举
     */
    public static RecommendType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RecommendType type : RecommendType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
