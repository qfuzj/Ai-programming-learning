package com.travel.advisor.dto.plan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** LLM 行程生成用的景点候选项 */
@Data
@Builder
public class TravelPlanAiScenicCandidate {

    private Long scenicId;

    private String name;

    private String category;

    private String level;

    private Double score;

    private String address;

    private String openTime;

    private BigDecimal ticketPrice;

    private String suggestedHours;
}
