package com.travel.advisor.vo.recommend;

import lombok.Data;

/**
 * 推荐项VO类，表示推荐系统中每个推荐结果的详细信息，包括推荐记录ID、景点ID、景点名称、封面图URL、推荐分数、推荐理由和推荐来源类型等字段，用于在前端展示个性化的推荐内容，帮助用户了解推荐结果的相关信息和推荐理由，提高用户的点击率和满意度
 */
@Data
public class RecommendItemVO {

    /**
     * 推荐记录ID
     */
    private Long recommendRecordId;

    /**
     * 推荐结果ID
     */
    private Long resultItemId;

    /**
     * 景点ID
     */
    private Long scenicId;

    /**
     * 景点名称
     */
    private String scenicName;

    /**
     * 景点封面图URL
     */
    private String coverImage;

    /**
     * 景区推荐分数
     */
    private Double score;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 推荐来源类型（如：协同过滤、内容推荐、混合推荐等）
     */
    private String sourceType;

    /**
     * 推荐算法计算的最终得分，用于排序和展示
     */
    private Double rankScore;
}
