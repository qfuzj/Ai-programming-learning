package com.travel.advisor.dto.plan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * AI 生成旅行计划 DTO，包含用户输入的旅行计划生成参数，供 TravelPlanAiService 使用
 */
@Data
public class TravelPlanAiGenerateDTO {

    @NotBlank(message = "目的地不能为空")
    private String destination;

    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数至少为 1 天")
    @Max(value = 15, message = "天数不能超过 15 天")
    private Integer days;

    @NotNull(message = "出发日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private BigDecimal budget;

    private String companionType;

    private String travelStyle;

    private List<String> preferredTags;
}
