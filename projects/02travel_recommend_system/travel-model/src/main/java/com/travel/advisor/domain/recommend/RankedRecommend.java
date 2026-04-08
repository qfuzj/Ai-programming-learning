package com.travel.advisor.domain.recommend;

import com.travel.advisor.entity.ScenicSpot;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 排序后的推荐结果对象，包含景点信息、综合得分和来源类型集合。
 */
@Data
@Builder
public class RankedRecommend {

    private ScenicSpot scenicSpot;

    /**
     * 综合得分，基于多个召回策略的基础分数累加计算得出，代表了该景点的推荐优先级。
     */
    private BigDecimal rankScore;

    /**
     * 来源类型集合，表示该景点是通过哪些召回策略命中的，例如“基于标签”、“基于地理位置”、“基于用户行为”等。这个字段可以帮助前端展示推荐理由或者进行后续的分析和优化。
     */
    private Set<String> sourceTypes;
}
