package com.travel.advisor.dto.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TravelPlanCreateDTO {

    @NotBlank(message = "行程标题不能为空")
    private String title;

    /**
     * 封面图URL
     */
    private String coverImage;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "总天数不能为空")
    private Integer totalDays;

    /**
     * 目的地所在的行政区ID（如城市或省份），用于推荐相关景点和活动
     */
    private Long destinationRegionId;

    /**
     * 行程描述，用户可以填写对行程的整体规划、期待等信息，帮助系统更好地推荐相关内容
     */
    private String description;

    /**
     * 预计预算，单位为元，用户可以填写一个大致的预算范围，系统可以根据预算推荐适合的景点、住宿和餐饮选项
     */
    private BigDecimal estimatedBudget;

    /**
     * 同行人信息，用户可以填写同行人的姓名或关系（如朋友、家人等），系统可以根据同行人信息推荐适合的活动和景点
     */
    private String travelCompanion;

    private Integer isPublic;

    /**
     * 状态：0 草稿 1 正常 2 已完成
     */
    private Integer status;
}
