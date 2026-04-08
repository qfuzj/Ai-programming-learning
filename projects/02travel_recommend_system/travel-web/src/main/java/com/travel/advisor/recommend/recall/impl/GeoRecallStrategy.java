package com.travel.advisor.recommend.recall.impl;

import com.travel.advisor.domain.recommend.RecallContext;
import com.travel.advisor.recommend.recall.RecallStrategy;
import com.travel.advisor.domain.recommend.RecallCandidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 地理召回策略：根据用户浏览历史中出现频率最高的地区，推荐该地区的景点
 */
@Component
@RequiredArgsConstructor
public class GeoRecallStrategy implements RecallStrategy {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    @Override
    public String strategyName() {
        return "GEO";
    }

    @Override
    public List<RecallCandidate> recall(RecallContext context) {
        Long userId = context.getUserId();
        // 如果用户未登录，则无法获取浏览历史，返回空列表
        if (userId == null) {
            return Collections.emptyList();
        }

        // 获取用户最近的浏览历史，限制在80条以内，避免过多历史数据影响性能
        List<UserBrowseHistory> browseHistories = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, userId)
                .orderByDesc(UserBrowseHistory::getBrowseTime)
                .last("limit 80"));

        // 如果用户没有浏览历史，则无法进行地理召回，返回空列表
        if (browseHistories.isEmpty()) {
            return Collections.emptyList();
        }

        // 从浏览历史中提取去重后的景点ID列表，并查询对应的景点信息，构建景点ID到景点对象的映射
        List<Long> scenicIds = browseHistories.stream().map(UserBrowseHistory::getScenicSpotId).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectBatchIds(scenicIds).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, Function.identity(), (left, right) -> left));

        // 统计每个地区在浏览历史中出现的次数，构建地区ID到出现次数的映射
        Map<Long, Long> regionCounter = browseHistories.stream()
                // 根据浏览历史中的景点ID获取对应的景点对象，过滤掉无法找到的景点
                .map(item -> scenicMap.get(item.getScenicSpotId()))
                .filter(Objects::nonNull)
                // 从景点对象中提取地区ID，过滤掉地区ID为null的景点
                .map(ScenicSpot::getRegionId)
                .filter(Objects::nonNull)
                // 统计每个地区ID出现的次数，构建地区ID到出现次数的映射
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 从地区出现次数的映射中找到出现次数最多的地区ID，作为目标地区
        Long targetRegion = regionCounter.entrySet().stream()
                // 找到出现次数最多的地区ID，如果没有找到则返回null
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        // 如果没有找到目标地区，则无法进行地理召回，返回空列表
        if (targetRegion == null) {
            return Collections.emptyList();
        }

        // 查询目标地区的景点列表，按照评分和收藏数降序排序，限制在20条以内，构建召回候选列表
        List<ScenicSpot> regionSpots = scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getRegionId, targetRegion)
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getScore)
                .orderByDesc(ScenicSpot::getFavoriteCount)
                .last("limit 20"));

        // 将目标地区的景点列表转换为召回候选列表，设置基础分数为0.8，返回结果
        return regionSpots.stream().map(item -> RecallCandidate.builder()
                .scenicId(item.getId())
                .sourceType(strategyName())
                .baseScore(BigDecimal.valueOf(0.8D))
                .build()).toList();
    }
}
