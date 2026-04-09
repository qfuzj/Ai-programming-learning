package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 平台日统计实体
 */
@Data
@TableName("stat_platform_daily")
public class StatPlatformDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 新增用户数
     */
    private Integer newUserCount;

    /*
     * 活跃用户数 （当日有行为）
     */
    private Integer activeUserCount;

    /**
     * 总浏览量
     */
    private Long totalPv;

    /**
     * 总独立访客数
     */
    private Integer totalUv;

    /**
     * 景点浏览量
     */
    private Integer scenicViewCount;

    /**
     * 景点收藏量
     */
    private Integer favoriteCount;

    /**
     * 评价数
     */
    private Integer reviewCount;

    /**
     * 创建旅行计划数
     */
    private Integer travelPlanCount;

    /**
     * 推荐请求数
     */
    private Integer recommendRequestCount;

    /**
     * LLM调用次数
     */
    private Integer llmCallCount;

    /**
     * LLM调用总成本
     */
    private BigDecimal llmCostAmount;

    /**
     * LLM调用总消耗的Token数
     */
    private Long llmTotalTokens;

    private LocalDateTime createTime;
}
