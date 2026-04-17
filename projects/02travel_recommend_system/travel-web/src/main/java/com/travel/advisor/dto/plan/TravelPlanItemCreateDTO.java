package com.travel.advisor.dto.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 行程计划项创建 DTO
 */
@Data
public class TravelPlanItemCreateDTO {

    @NotNull(message = "dayNo不能为空")
    @Min(value = 1, message = "dayNo 不能小于 1")
    private Integer dayNo;
    
    private Long scenicSpotId;
    
    private Integer sortOrder;

    /**
     * 类型：1 景点 2 餐饮 3 住宿 4 交通 5 自定义
     */
    private Integer itemType;

    @NotBlank(message = "行程项标题不能为空")
    private String title;

    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal estimatedCost;

    private String notes;
}
