package com.travel.advisor.domain.recommend;

import lombok.Builder;
import lombok.Data;

/**
 * 召回上下文对象，包含用户ID、景点ID、分页信息等，用于在推荐系统中传递召回阶段所需的上下文信息，以便根据这些信息进行个性化的推荐结果生成
 */
@Data
@Builder
public class RecallContext {

    private Long userId;

    private Long scenicId;

    private Integer pageNum;

    private Integer pageSize;
}
