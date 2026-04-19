package com.travel.advisor.dto.plan;

import com.travel.advisor.common.enums.TravelCompanionType;
import com.travel.advisor.common.enums.TravelStyle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    /** 出游同伴类型，取值见 {@link TravelCompanionType}，可为空 */
    @Pattern(regexp = TravelCompanionType.CODE_PATTERN, message = "出游同伴类型不合法")
    private String companionType;

    /** 旅行风格，取值见 {@link TravelStyle}，可为空 */
    @Pattern(regexp = TravelStyle.CODE_PATTERN, message = "旅行风格不合法")
    private String travelStyle;

    private List<String> preferredTags;
}
