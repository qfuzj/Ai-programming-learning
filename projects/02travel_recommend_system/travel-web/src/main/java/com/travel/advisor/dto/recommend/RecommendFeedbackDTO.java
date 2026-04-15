package com.travel.advisor.dto.recommend;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 推荐反馈DTO，用于接收用户对推荐结果的反馈信息，包括推荐记录ID、推荐结果ID和景点ID等字段，以便后续进行用户行为分析和推荐算法优化
 */
@Data
public class RecommendFeedbackDTO {

    /**
     * 推荐记录ID
     */
    @NotNull(message = "recommendRecordId不能为空")
    private Long recommendRecordId;

    /**
     * 推荐结果项ID
     */
    @NotNull(message = "resultItemId不能为空")
    private Long resultItemId;

    /**
     * 景点ID
     */
    @NotNull(message = "scenicId不能为空")
    private Long scenicId;
}
