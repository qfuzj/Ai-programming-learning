package com.travel.advisor.recommend;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RecommendReasonResult {

    private Map<Long, String> reasons;

    private Map<Long, RecommendReasonDetail> details;

    private Boolean llmUsed;

    private Long llmCallLogId;

    @Data
    @Builder
    public static class RecommendReasonDetail {

        private String reason;

        private String tone;

        private java.util.List<String> highlights;
    }
}
