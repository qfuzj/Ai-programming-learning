package com.travel.advisor.recommend;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 推荐理由构建器，根据推荐来源类型生成相应的推荐理由文本。
 */
@Component
public class RecommendReasonBuilder {

    public String build(Set<String> sourceTypes) {
        if (sourceTypes == null || sourceTypes.isEmpty()) {
            return "基于平台热度推荐";
        }
        if (sourceTypes.size() > 1) {
            return "综合您的偏好与热门趋势为您推荐";
        }
        if (sourceTypes.contains("TAG")) {
            return "基于您的兴趣标签偏好推荐";
        }
        if (sourceTypes.contains("GEO")) {
            return "基于您常浏览地区的相似景点推荐";
        }
        return "基于平台热度推荐";
    }
}
