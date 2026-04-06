package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 行程规划实体类
 */
@Data
@TableName("travel_plan")
public class TravelPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /**
     * 封面图
     */
    private String coverImage;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalDays;

    /**
     * 目的地所在的行政区划ID，关联region表
     */
    private Long destinationRegionId;

    /**
     * 行程描述
     */
    private String description;

    /**
     * 预计预算，单位：元
     */
    private BigDecimal estimatedBudget;

    /**
     * 同行人类型
     */
    private String travelCompanion;

    private Integer isPublic;

    private Integer viewCount;

    private Integer likeCount;

    /**
     * 来源：1 用户创建 2 LLM 生成 3 系统推荐
     */
    private Integer source;

    /**
     * 状态：0 草稿 1 正常 2 已完成
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
