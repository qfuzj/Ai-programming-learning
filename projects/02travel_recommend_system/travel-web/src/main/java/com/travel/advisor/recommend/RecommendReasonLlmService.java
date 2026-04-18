package com.travel.advisor.recommend;

import com.travel.advisor.domain.recommend.RankedRecommend;

import java.util.List;

public interface RecommendReasonLlmService {

    /**
     * 生成推荐理由的核心方法，接收用户ID、场景信息和候选景点列表，调用LLM生成推荐理由，并记录调用日志
      * 1. 构建LLM请求，整合场景信息和候选景点列表，形成清晰的指令，指导LLM生成针对每个候选景点的推荐理由
      * 2. 调用LLM获取推荐理由，并解析LLM返回的JSON结果，提取每个景点对应的推荐理由文本，形成景点ID到推荐理由的映射
      * 3. 记录LLM调用日志，保存请求和响应内容，以及调用结果状态，便于后续分析和优化推荐理由生成的效果
      * 4. 返回生成的推荐理由结果对象，包含景点ID到推荐理由的映射、是否使用LLM以及LLM调用日志ID等信息，供调用方使用
     */
    RecommendReasonResult generateReasons(Long userId, String scene, List<RankedRecommend> pageItems);
}
