package com.travel.advisor.recommend;

import com.travel.advisor.domain.recommend.RankedRecommend;

import java.util.List;

public interface RecommendReasonLlmService {

    /** 调用 LLM 生成推荐理由 */
    RecommendReasonResult generateReasons(Long userId, String scene, List<RankedRecommend> pageItems);
}
