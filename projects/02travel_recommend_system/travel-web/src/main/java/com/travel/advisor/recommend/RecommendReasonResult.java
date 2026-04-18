package com.travel.advisor.recommend;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RecommendReasonResult {

    private Map<Long, String> reasons;

    private Boolean llmUsed;

    private Long llmCallLogId;
}
