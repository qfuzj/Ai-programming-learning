package com.travel.advisor.recommend;

import com.travel.advisor.domain.recommend.RankedRecommend;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

public interface RecommendReasonLlmService {

    RecommendReasonResult generateReasons(Long userId, String scene, List<RankedRecommend> pageItems);

    /**
     * 推荐理由结果对象，包含景点ID到推荐理由的映射、是否使用LLM以及LLM调用日志ID等信息，用于返回给调用方
      * 1. reasons 字段是一个 Map，键为景点ID，值为对应的推荐理由文本，便于调用方根据景点ID获取推荐理由
      * 2. llmUsed 字段表示是否实际调用了LLM生成推荐理由，如果为 false 则说明使用了默认或缓存的推荐理由
      * 3. llmCallLogId 字段记录了本次LLM调用的日志ID，便于后续查询和分析LLM调用情况
      * 4. 通过这个类可以将推荐理由生成的结果封装成一个对象，方便调用方获取推荐理由和相关信息，同时也便于系统内部处理和记录推荐理由生成的过程和结果
     */
    @Data
    @Builder
    class RecommendReasonResult {

        private Map<Long, String> reasons;

        private Boolean llmUsed;

        private Long llmCallLogId;
    }
}
