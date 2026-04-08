package com.travel.advisor.recommend.recall.impl;

import com.travel.advisor.domain.recommend.RecallContext;
import com.travel.advisor.recommend.recall.RecallStrategy;
import com.travel.advisor.domain.recommend.RecallCandidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标签召回策略：基于用户历史行为（收藏、浏览、评论）涉及的景点标签，找回具有相似标签的其他景点作为推荐候选。
 */
@Component
@RequiredArgsConstructor
public class TagRecallStrategy implements RecallStrategy {

    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserReviewMapper userReviewMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;

    @Override
    public String strategyName() {
        return "TAG";
    }

    @Override
    public List<RecallCandidate> recall(RecallContext context) {
        Long userId = context.getUserId();
        // 前置校验：如果用户未登录，则无法基于用户行为进行标签召回，直接返回空结果。
        if (userId == null) {
            return Collections.emptyList();
        }

        // 1. 获取用户历史行为涉及的景点ID集合，作为标签召回的种子数据。我们可以从用户的收藏、浏览历史和评论中获取这些景点ID。
        Set<Long> behaviorScenicIds = new HashSet<>();

        // 收藏通常是用户行为中最具代表性的，因此我们可以优先获取用户的收藏数据，并适当增加数量限制，以获取更多的种子数据。
        List<UserFavorite> favorites = userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime)
                .last("limit 50"));
        favorites.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        // 浏览历史和评论可能比收藏更频繁，因此我们可以适当增加它们的数量限制，以获取更多的种子数据。
        List<UserBrowseHistory> browseHistories = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, userId)
                .orderByDesc(UserBrowseHistory::getBrowseTime)
                .last("limit 50"));
        browseHistories.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        // 评论可能比收藏和浏览更频繁，因此我们可以适当增加它们的数量限制，以获取更多的种子数据。
        List<UserReview> reviews = userReviewMapper.selectList(new LambdaQueryWrapper<UserReview>()
                .eq(UserReview::getUserId, userId)
                .orderByDesc(UserReview::getCreateTime)
                .last("limit 30"));
        reviews.forEach(item -> behaviorScenicIds.add(item.getScenicSpotId()));

        // 前置校验：如果用户没有任何历史行为数据，则无法基于标签进行召回，直接返回空结果。
        if (behaviorScenicIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 基于用户历史行为涉及的景点ID集合，查询这些景点对应的景点标签关联表，获取这些景点的标签ID集合。我们可以通过批量查询的方式，一次性获取所有相关景点的标签数据。
        List<ScenicSpotTag> seedTags = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .in(ScenicSpotTag::getScenicSpotId, behaviorScenicIds));
        Set<Long> tagIds = new HashSet<>();
        seedTags.forEach(item -> tagIds.add(item.getTagId()));

        // 前置校验：如果用户历史行为涉及的景点没有任何标签数据，则无法基于标签进行召回，直接返回空结果。
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 基于标签ID集合，查询景点标签关联表，找回具有相似标签的其他景点ID集合。我们可以通过批量查询的方式，一次性获取所有相关标签的景点数据，并进行去重和过滤，排除掉用户历史行为中已经涉及过的景点。
        List<ScenicSpotTag> recallTags = scenicSpotTagMapper.selectList(new LambdaQueryWrapper<ScenicSpotTag>()
                .in(ScenicSpotTag::getTagId, tagIds)
                .last("limit 200"));

        // 前置校验：如果没有找到任何具有相似标签的景点，则无法基于标签进行召回，直接返回空结果。
        if (recallTags.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 将找回的景点ID集合转换为召回候选列表，设置基础分数和来源类型等信息。我们可以为每个候选景点设置一个默认的基础分数，例如1.0，并将来源类型设置为当前策略的名称，以便后续的排序和分析。
        return recallTags.stream()
                .map(ScenicSpotTag::getScenicSpotId)
                // 过滤掉用户历史行为中已经涉及过的景点，避免重复推荐。
                .filter(id -> !behaviorScenicIds.contains(id))
                .distinct()
                .map(id -> RecallCandidate.builder()
                        .scenicId(id)
                        .sourceType(strategyName())
                        .baseScore(BigDecimal.valueOf(1.0D))
                        .build())
                .toList();
    }
}
