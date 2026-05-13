package com.travel.advisor.recommend.recall.impl;

import com.travel.advisor.recommend.recall.RecallStrategy;
import com.travel.advisor.domain.recommend.RecallCandidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.entity.Region;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.User;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 地理召回策略：优先按用户资料地区推荐，未设置地区时按常浏览地区推荐
 */
@Component
@RequiredArgsConstructor
public class GeoRecallStrategy implements RecallStrategy {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final UserMapper userMapper;
    private final RegionMapper regionMapper;

    @Override
    public String strategyName() {
        return "GEO";
    }

    @Override
    public List<RecallCandidate> recall(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        Set<Long> targetRegionIds = resolveProfileRegionIds(userId);
        if (targetRegionIds.isEmpty()) {
            Long browseRegionId = resolveBrowseRegionId(userId);
            if (browseRegionId == null) {
                return Collections.emptyList();
            }
            targetRegionIds = Set.of(browseRegionId);
        }

        List<ScenicSpot> regionSpots = scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                .in(ScenicSpot::getRegionId, targetRegionIds)
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getScore)
                .orderByDesc(ScenicSpot::getFavoriteCount)
                .last("limit 20"));

        return regionSpots.stream().map(item -> RecallCandidate.builder()
                .scenicId(item.getId())
                .sourceType(strategyName())
                .baseScore(0.8D)
                .build()).toList();
    }

    private Set<Long> resolveProfileRegionIds(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRegionId() == null) {
            return Collections.emptySet();
        }
        Set<Long> regionIds = new LinkedHashSet<>();
        regionIds.add(user.getRegionId());
        collectDescendantRegionIds(user.getRegionId(), regionIds);
        return regionIds;
    }

    private void collectDescendantRegionIds(Long parentId, Set<Long> regionIds) {
        List<Region> children = regionMapper.selectList(new LambdaQueryWrapper<Region>()
                .eq(Region::getParentId, parentId));
        for (Region child : children) {
            if (child.getId() != null && regionIds.add(child.getId())) {
                collectDescendantRegionIds(child.getId(), regionIds);
            }
        }
    }

    private Long resolveBrowseRegionId(Long userId) {
        List<UserBrowseHistory> browseHistories = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, userId)
                .orderByDesc(UserBrowseHistory::getBrowseTime)
                .last("limit 80"));

        if (browseHistories.isEmpty()) {
            return null;
        }

        List<Long> scenicIds = browseHistories.stream().map(UserBrowseHistory::getScenicSpotId).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectBatchIds(scenicIds).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, Function.identity(), (left, right) -> left));

        Map<Long, Long> regionCounter = browseHistories.stream()
                .map(item -> scenicMap.get(item.getScenicSpotId()))
                .filter(Objects::nonNull)
                .map(ScenicSpot::getRegionId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return regionCounter.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
