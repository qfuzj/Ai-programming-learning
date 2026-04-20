package com.travel.advisor.dto.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** LLM 行程草稿解析结构 */
@Data
public class TravelPlanAiDraftPayload {

    private String title;

    private String description;

    private List<DayPayload> days;

    @Data
    public static class DayPayload {

        private Integer dayNo;

        private List<ItemPayload> items;
    }

    @Data
    public static class ItemPayload {

        private Integer itemType;

        private Long scenicSpotId;

        private String title;

        private String description;

        private String startTime;

        private String endTime;

        private String location;

        private BigDecimal estimatedCost;

        private String notes;
    }
}
