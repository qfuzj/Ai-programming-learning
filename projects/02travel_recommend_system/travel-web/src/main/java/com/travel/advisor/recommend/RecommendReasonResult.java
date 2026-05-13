package com.travel.advisor.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendReasonResult {

    private Map<Long, String> reasons;

    private Map<Long, RecommendReasonDetail> details;

    private Boolean llmUsed;

    private Long llmCallLogId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendReasonDetail {

        private String reason;

        private String tone;

        private List<String> highlights;
    }
}
