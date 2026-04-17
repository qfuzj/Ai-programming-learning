package com.travel.advisor.recommend.recall.impl;

import com.travel.advisor.domain.recommend.RecallContext;
import com.travel.advisor.recommend.recall.RecallStrategy;
import com.travel.advisor.domain.recommend.RecallCandidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 基于景点热度的召回策略实现类，负责从数据库中查询热度较高的景点并将其转换为召回候选对象列表。
 * 该策略通过景点的收藏数、浏览数和评分来评估热度，优先推荐热度较高的景点。
 */
@Component
@RequiredArgsConstructor
public class HotRecallStrategy implements RecallStrategy {

    private final ScenicSpotMapper scenicSpotMapper;

    @Override
    public String strategyName() {
        return "HOT";
    }

    @Override
    public List<RecallCandidate> recall(RecallContext context) {
        List<ScenicSpot> spots = scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
            .eq(ScenicSpot::getStatus, 1)
            .orderByDesc(ScenicSpot::getFavoriteCount)
            .orderByDesc(ScenicSpot::getViewCount)
            .orderByDesc(ScenicSpot::getScore)
            .last("limit 30"));
        if (spots.isEmpty()) {
            return Collections.emptyList();
        }
        return spots.stream().map(item -> RecallCandidate.builder()
            .scenicId(item.getId())
            .sourceType(strategyName())
            .baseScore(Double.valueOf(0.6D))
            .build()).toList();
    }
}
