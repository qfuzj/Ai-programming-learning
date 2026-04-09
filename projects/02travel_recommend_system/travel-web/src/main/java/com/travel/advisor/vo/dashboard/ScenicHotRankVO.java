package com.travel.advisor.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点热门排行榜VO类
 */
@Data
public class ScenicHotRankVO {

    /**
     * 景点ID
     */
    private Long scenicId;

    /**
     * 景点名称
     */
    private String scenicName;

    /**
     * 浏览量
     */
    private Integer pvCount;

    /**
     * 独立访客数
     */
    private Integer uvCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 评论数
     */
    private Integer reviewCount;

    /**
     * 推荐展示数
     */
    private Integer recommendShowCount;

    /**
     * 推荐点击数
     */
    private Integer recommendClickCount;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;
}
