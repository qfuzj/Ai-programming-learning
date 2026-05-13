package com.travel.advisor.recommend.recall;

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
     * 基于用户ID生成召回候选项列表。
     * @param userId 当前用户ID
     * @return 召回候选项列表
     */
    List<RecallCandidate> recall(Long userId);
}
