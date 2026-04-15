package com.travel.advisor.domain.recommend;

import lombok.Builder;
import lombok.Data;



/**
 * 召回候选类，表示推荐系统中被召回的候选项，包含景点ID、推荐来源类型和基础分数等字段，用于在后续的排序和融合阶段进行权重调整和最终得分计算，以提高推荐结果的相关性和用户满意度
 */
@Data
@Builder
public class RecallCandidate {

    private Long scenicId;

    /**
     * 推荐来源类型（如：协同过滤、内容推荐、混合推荐等），用于区分不同的推荐算法或策略，以便在后续的排序和融合阶段进行权重调整和效果分析
     */
    private String sourceType;

    /**
     * 基础分数，由具体的召回算法计算得出，数值越大表示推荐结果越相关，范围通常在0到1之间，后续的排序和融合阶段可以根据这个基础分数进行权重调整和最终得分计算
     */
    private Double baseScore;
}
