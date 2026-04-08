package com.travel.advisor.service;

import com.travel.advisor.dto.recommend.RecommendFeedbackDTO;

/**
 * 推荐反馈服务接口，定义了用户对推荐结果的反馈操作，包括曝光、点击和收藏等方法，以便在推荐系统中收集用户行为数据，进行用户行为分析和推荐算法优化
 */
public interface RecommendFeedbackService {

    /**
     * 记录推荐结果的曝光行为，即用户看到推荐结果但未进行点击或收藏等操作时的反馈信息，通常用于分析推荐结果的展示效果和用户兴趣偏好
     * @param dto 推荐反馈DTO，包含推荐记录ID、推荐结果ID和景点ID等字段，用于标识具体的推荐结果和用户行为，以便后续进行数据分析和算法优化
     */
    void exposure(RecommendFeedbackDTO dto);

    /**
     * 记录推荐结果的点击行为，即用户对推荐结果进行点击操作时的反馈信息，通常用于分析推荐结果的吸引力和用户兴趣偏好，以及优化推荐算法以提高点击率和用户满意度
     * @param dto 推荐反馈DTO，包含推荐记录ID、推荐结果ID和景点ID等字段，用于标识具体的推荐结果和用户行为，以便后续进行数据分析和算法优化
     */
    void click(RecommendFeedbackDTO dto);

    /**
     * 记录推荐结果的收藏行为，即用户对推荐结果进行收藏操作时的反馈信息，通常用于分析推荐结果的吸引力和用户兴趣偏好，以及优化推荐算法以提高用户满意度和忠诚度
     * @param dto 推荐反馈DTO，包含推荐记录ID、推荐结果ID和景点ID等字段，用于标识具体的推荐结果和用户行为，以便后续进行数据分析和算法优化
     */
    void favorite(RecommendFeedbackDTO dto);
}
