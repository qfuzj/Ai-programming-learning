package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 景点日统计实体
 */
@Data
@TableName("stat_scenic_daily")
public class StatScenicDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点ID
     */
    private Long scenicSpotId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 页面浏览量（PV）
     */
    private Integer pvCount;

    /**
     * 独立访客数（UV）
     */
    private Integer uvCount;

    /**
     * 当日新增收藏数
     */
    private Integer favoriteCount;

    /**
     * 当日新增评论数
     */
    private Integer reviewCount;

    /**
     * 当日平均评分
     */
    private BigDecimal avgRating;

    /**
     * 推荐曝光次数
     */
    private Integer recommendShowCount;

    /**
     * 推荐点击次数
     */
    private Integer recommendClickCount;

    /**
     * 搜索命中次数
     */
    private Integer searchCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

    private LocalDateTime createTime;
}
