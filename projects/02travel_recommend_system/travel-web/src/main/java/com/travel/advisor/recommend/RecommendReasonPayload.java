package com.travel.advisor.recommend;

import lombok.Data;

import java.util.List;

@Data
public class RecommendReasonPayload {

    private List<RecommendReasonItem> items;

    @Data
    public static class RecommendReasonItem {

        private Long scenicId;

        private String reason;
    }
}
