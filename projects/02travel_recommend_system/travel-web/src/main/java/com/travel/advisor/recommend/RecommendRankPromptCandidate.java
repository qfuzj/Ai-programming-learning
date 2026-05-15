package com.travel.advisor.recommend;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class RecommendRankPromptCandidate {

    private Long scenicId;

    private String name;

    private String regionName;

    private String category;

    private String level;

    private BigDecimal ticketPrice;

    private String bestSeason;

    private String suggestedHours;

    private String description;

    private List<String> tags;

    private Set<String> sourceTypes;

    private Double rankScore;
}
