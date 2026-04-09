package com.travel.advisor.vo.dashboard;

import lombok.Data;

/**
 * 仪表盘概览数据VO，包含平台的关键统计指标，用于在管理员后台展示整体运营情况。
 */
@Data
public class DashboardOverviewVO {

    /**
     * 平台总用户数
     */
    private Long totalUsers;
    /**
     * 平台总景点数
     */
    private Long totalScenicSpots;
    /**
     * 平台总评论数
     */
    private Long totalReviews;
    /**
     * 平台总旅行计划数
     */
    private Long totalTravelPlans;
    /**
     * 平台总推荐请求数
     */
    private Long totalRecommendRequests;
    /**
     * 平台总LLM调用次数
     */
    private Long totalLlmCalls;
    /**
     * 平台总浏览数
     */
    private Long totalBrowseCount;
    /**
     * 平台总收藏数
     */
    private Long totalFavoriteCount;
}
