package com.travel.advisor.recommend.recall;

import com.travel.advisor.domain.recommend.RecallContext;
import com.travel.advisor.domain.recommend.RecallCandidate;

import java.util.List;

/**
 * 召回策略接口，定义了召回候选项的生成方法。
 */
public interface RecallStrategy {

    /**
     * 获取召回策略的名称，用于标识不同的召回策略。
     * @return 召回策略的名称
     */
    String strategyName();

    /**
     * 根据给定的召回上下文生成召回候选项列表。
     * @param context 召回上下文，包含用户信息、请求参数等相关数据
     * @return 召回候选项列表，根据上下文生成的候选项可能包含不同的属性和权重
     */
    List<RecallCandidate> recall(RecallContext context);
}
