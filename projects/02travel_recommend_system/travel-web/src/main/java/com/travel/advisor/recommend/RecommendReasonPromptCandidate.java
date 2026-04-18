package com.travel.advisor.recommend;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RecommendReasonPromptCandidate {

    private Long scenicId;

    private String name;

    private String regionName;

    private String category;

    private String level;

    private Double score;

    private Set<String> sourceTypes;

    private Double rankScore;
}
