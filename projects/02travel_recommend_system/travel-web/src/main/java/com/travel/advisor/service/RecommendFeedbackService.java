package com.travel.advisor.service;

import com.travel.advisor.dto.recommend.RecommendFeedbackDTO;

/** 推荐反馈服务 */
public interface RecommendFeedbackService {

    /** 记录曝光反馈 */
    void exposure(RecommendFeedbackDTO dto);

    /** 记录点击反馈 */
    void click(RecommendFeedbackDTO dto);

    /** 记录收藏反馈 */
    void favorite(RecommendFeedbackDTO dto);
}
