package com.travel.advisor.vo.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Data
public class TravelPlanDetailVO {

    private Long id;

    private String title;

    private String coverImage;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalDays;

    private Long destinationRegionId;

    private String description;

    private BigDecimal estimatedBudget;

    private String travelCompanion;

    private Integer isPublic;

    private Integer source;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 行程每天的详细安排列表，每天包含多个行程项（如景点、餐饮、住宿等），按照天数和顺序排列
     */
    private List<TravelPlanDayVO> days = Collections.emptyList();

    /**
     * TravelPlanDayVO表示行程中的一天，包含当天的行程项列表。每个行程项（TravelPlanItemVO）包含了具体的活动信息，如景点名称、时间安排、位置等。通过这种结构设计，用户可以清晰地看到每天的行程安排，并且系统可以根据这些信息提供更精准的推荐和优化建议。
     */
    @Data
    public static class TravelPlanDayVO {
        private Integer dayNo;
        /**
         * 当天的行程项列表，按照sortOrder字段排序，确保用户看到的行程安排是按照计划的顺序展示的
         */
        private List<TravelPlanItemVO> items = Collections.emptyList();
    }

    @Data
    public static class TravelPlanItemVO {
        private Long id;
        private Long scenicSpotId;
        private Integer dayNo;
        private Integer sortOrder;
        private Integer itemType;
        private String title;
        private String description;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private BigDecimal estimatedCost;
        private String notes;
        private LocalDateTime createTime;
    }
}
