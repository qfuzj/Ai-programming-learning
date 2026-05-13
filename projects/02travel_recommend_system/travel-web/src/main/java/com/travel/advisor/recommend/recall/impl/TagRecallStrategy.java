package com.travel.advisor.recommend.recall.impl;

import com.travel.advisor.recommend.recall.RecallStrategy;
import com.travel.advisor.domain.recommend.RecallCandidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.entity.UserPreferenceTag;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.mapper.UserPreferenceTagMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 标签召回策略：优先基于用户手动偏好标签召回，偏好为空时降级到历史行为标签。
 */
@Component
@RequiredArgsConstructor
public class TagRecallStrategy implements RecallStrategy {

    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final UserPreferenceTagMapper userPreferenceTagMapper;
    private final TagMapper tagMapper;

    @Override
    public String strategyName() {
        return "TAG";
    }

    @Override
    public List<RecallCandidate> recall(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        Set<Long> behaviorScenicIds = loadBehaviorScenicIds(userId);
        Set<Long> tagIds = loadPreferenceTagIds(userId);
        boolean explicitPreference = !tagIds.isEmpty();
        if (!explicitPreference) {
            tagIds = loadBehaviorTagIds(behaviorScenicIds);
        }
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScenicSpotTag> recallTags = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .in(ScenicSpotTag::getTagId, tagIds)
                .last("limit 500"));

        if (recallTags.isEmpty()) {
            return Collections.emptyList();
        }

        return recallTags.stream()
                .map(ScenicSpotTag::getScenicSpotId)
                .filter(id -> explicitPreference || !behaviorScenicIds.contains(id))
                .distinct()
                .map(id -> RecallCandidate.builder()
                        .scenicId(id)
                        .sourceType(strategyName())
                        .baseScore(explicitPreference ? 1.2D : 1.0D)
                        .build())
                .toList();
    }

    private Set<Long> loadPreferenceTagIds(Long userId) {
        List<UserPreferenceTag> preferenceTags = userPreferenceTagMapper.selectList(new LambdaQueryWrapper<UserPreferenceTag>()
                .eq(UserPreferenceTag::getUserId, userId));
        Set<Long> tagIds = new HashSet<>();
        List<Long> preferenceTagIds = preferenceTags.stream()
                .map(UserPreferenceTag::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (preferenceTagIds.isEmpty()) {
            return tagIds;
        }

        List<Tag> tags = tagMapper.selectBatchIds(preferenceTagIds);
        for (Tag tag : tags) {
            if (tag.getId() == null) {
                continue;
            }
            tagIds.add(tag.getId());
            if (!"PREFERENCE".equals(tag.getScope()) || tag.getName() == null || tag.getName().isBlank()) {
                continue;
            }
            tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getStatus, 1)
                    .eq(Tag::getName, tag.getName())
                    .and(w -> w.eq(Tag::getScope, "SCENIC").or().eq(Tag::getScope, "BOTH")))
                    .forEach(mappedTag -> tagIds.add(mappedTag.getId()));
        }
        return tagIds;
    }

    private Set<Long> loadBehaviorScenicIds(Long userId) {
        Set<Long> behaviorScenicIds = new HashSet<>();

        List<UserFavorite> favorites = userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime)
                .last("limit 50"));
        favorites.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        List<UserBrowseHistory> browseHistories = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, userId)
                .orderByDesc(UserBrowseHistory::getBrowseTime)
                .last("limit 50"));
        browseHistories.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        List<UserReview> reviews = userReviewMapper.selectList(new LambdaQueryWrapper<UserReview>()
                .eq(UserReview::getUserId, userId)
                .orderByDesc(UserReview::getCreateTime)
                .last("limit 30"));
        reviews.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        return behaviorScenicIds;
    }

    private Set<Long> loadBehaviorTagIds(Set<Long> behaviorScenicIds) {
        if (behaviorScenicIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<ScenicSpotTag> seedTags = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .in(ScenicSpotTag::getScenicSpotId, behaviorScenicIds));
        Set<Long> tagIds = new HashSet<>();
        seedTags.forEach(item -> tagIds.add(item.getTagId()));
        return tagIds;
    }
}
