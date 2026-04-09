package com.travel.advisor.vo.dashboard;

import lombok.Data;

import java.util.List;

/**
 * 推荐分析VO
 */
@Data
public class RecommendAnalysisVO {

    /**
     * 总推荐请求次数
     */
    private Long totalRecommendRequests;

    /**
     * 总推荐点击次数
     */
    private Long totalRecommendClicks;

    /**
     * 总推荐收藏次数
     */
    private Long totalRecommendFavorites;

    /**
     * 推荐点击率 = 点击次数 / 请求次数
     */
    private Double clickRate;

    /**
     * 推荐收藏率 = 收藏次数 / 请求次数
     */
    private Double favoriteRate;

    /**
     * 日期列表，格式 yyyy-MM-dd，作为趋势图 X 轴
     * 示例：["2026-01-01", "2026-01-02", "2026-01-03"]
     */
    private List<String> dates;

    /**
     * 每日推荐请求次数，与 dates 下标一一对应，作为趋势图 Y 轴
     * 示例：[120, 98, 135]
     */
    private List<Long> requestCounts;

    /**
     * 每日推荐点击次数，与 dates 下标一一对应，作为趋势图 Y 轴
     * 示例：[30, 20, 40]
     */
    private List<Long> clickCounts;

    /**
     * 每日推荐收藏次数，与 dates 下标一一对应，作为趋势图 Y 轴
     * 示例：[10, 5, 15]
     */
    private List<Long> favoriteCounts;
}
