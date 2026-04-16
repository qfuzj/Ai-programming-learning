package com.travel.advisor.vo.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * 行程计划详情 VO（View Object）
 *
 * 用于前端展示完整的行程计划信息，包含基础信息 + 按天拆分的行程结构。
 *
 * 数据结构设计：
 * Plan（行程） → Day（天） → Item（具体行程项）
 *
 * 该结构支持：
 * - 行程可视化展示（按天分组）
 * - 行程编辑与排序（基于 dayNo + sortOrder）
 * - 后续扩展（如推荐优化、地图展示等）
 */
@Data
public class TravelPlanDetailVO {

    /**
     * 行程ID（唯一标识）
     */
    private Long id;

    /**
     * 行程标题
     */
    private String title;
    /**
     * 行程封面图URL
     */
    private String coverImage;
    /**
     * 行程开始日期
     */
    private LocalDate startDate;
    /**
     * 行程结束日期
     */
    private LocalDate endDate;

    /**
     * 总天数（通常 = endDate - startDate + 1）
     */
    private Integer totalDays;

    /**
     * 目的地地区ID（用于关联区域/城市）
     */
    private Long destinationRegionId;

    /**
     * 行程描述（用户输入或AI生成）
     */
    private String description;

    /**
     * 预估预算（单位：元）
     */
    private BigDecimal estimatedBudget;

    /**
     * 出行同伴类型（如：solo/couple/family/friends）
     */
    private String travelCompanion;

    /**
     * 是否公开（0-私有，1-公开）
     */
    private Integer isPublic;

    /**
     * 行程来源（1-用户创建，2-AI生成，3-模板复制等）
     */
    private Integer source;

    /**
     * 行程状态（如：草稿、已完成、已发布等）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 行程天列表（按 dayNo 升序）
     */
    private List<TravelPlanDayVO> days = Collections.emptyList();

    /**
     * 行程“天”维度结构
     *
     * 表示某一天的所有行程安排。
     */
    @Data
    public static class TravelPlanDayVO {

        /**
         * 第几天（从 1 开始）
         */
        private Integer dayNo;

        /**
         * 当天的行程项列表（按 sortOrder 升序）
         */
        private List<TravelPlanItemVO> items = Collections.emptyList();
    }

    /**
     * 行程项（最小粒度）
     *
     * 表示一天中的一个具体活动，例如：
     * - 景点游览
     * - 用餐
     * - 酒店入住
     * - 交通安排
     */
    @Data
    public static class TravelPlanItemVO {

        /**
         * 行程项ID
         */
        private Long id;

        /**
         * 关联景点ID（可为空，例如餐饮/交通）
         */
        private Long scenicSpotId;

        /**
         * 所属天数
         */
        private Integer dayNo;

        /**
         * 排序序号（用于控制展示顺序）
         */
        private Integer sortOrder;

        /**
         * 行程项类型
         * （如：1-景点 2-餐饮 3-住宿 4-交通 5-其他）
         */
        private Integer itemType;

        /**
         * 行程项标题
         */
        private String title;

        /**
         * 行程项描述
         */
        private String description;

        /**
         * 开始时间（可为空）
         */
        private LocalTime startTime;

        /**
         * 结束时间（可为空）
         */
        private LocalTime endTime;

        /**
         * 地点名称（文本地址）
         */
        private String location;

        /**
         * 经度（WGS84）
         */
        private BigDecimal longitude;

        /**
         * 纬度（WGS84）
         */
        private BigDecimal latitude;

        /**
         * 预估费用（单位：元）
         */
        private BigDecimal estimatedCost;

        /**
         * 备注信息（用户补充）
         */
        private String notes;

        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
    }
}