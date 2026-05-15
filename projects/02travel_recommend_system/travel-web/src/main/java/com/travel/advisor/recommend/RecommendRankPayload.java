package com.travel.advisor.recommend;

import lombok.Data;

import java.util.List;

@Data
public class RecommendRankPayload {

    private List<Long> orderedScenicIds;
}
