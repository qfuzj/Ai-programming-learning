package com.travel.advisor.service.portrait.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.entity.UserBrowseHistory;
import com.travel.advisor.entity.UserFavorite;
import com.travel.advisor.entity.UserPreferenceTag;
import com.travel.advisor.entity.UserProfile;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.mapper.UserBrowseHistoryMapper;
import com.travel.advisor.mapper.UserFavoriteMapper;
import com.travel.advisor.mapper.UserPreferenceTagMapper;
import com.travel.advisor.mapper.UserProfileMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.service.portrait.PortraitSummaryContext;
import com.travel.advisor.service.portrait.UserPortraitAnalyzer;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPortraitAnalyzerImpl implements UserPortraitAnalyzer {

    private static final int RECENT_BROWSE_LIMIT = 30;
    private static final double BROWSE_WEIGHT = 1D;
    private static final double FAVORITE_WEIGHT = 3D;
    private static final double REVIEW_WEIGHT = 5D;
    private static final int INTEREST_KEYWORDS_TOP_N = 8;
    private static final Duration SUMMARY_TTL = Duration.ofHours(24);
    private static final String SUMMARY_CACHE_PREFIX = "portrait:summary:";

    private static final String SUMMARY_SYSTEM_PROMPT = """
            你是用户画像撰写助手。
            根据给定的旅行画像数据，写一段 30 到 50 个中文字符的自然摘要，介绍用户的旅行偏好与风格。
            语气亲切自然，不使用括号、不堆叠形容词、不输出 Markdown，仅输出一段纯文本。
            """;

    private final UserProfileMapper userProfileMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserReviewMapper userReviewMapper;
    private final UserPreferenceTagMapper userPreferenceTagMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final TagMapper tagMapper;
    private final RedisUtils redisUtils;
    private final LlmGateway llmGateway;
    private final LlmProperties llmProperties;

    @Override
    public void analyzeAndPersist(Long userId) {
        if (userId == null) {
            return;
        }
        Map<Long, Double> scenicScoreMap = aggregateScenicScores(userId);
        List<ScenicSpot> scenics = scenicScoreMap.isEmpty()
                ? List.of()
                : loadScenics(scenicScoreMap.keySet());

        String travelStyle = inferTravelStyle(scenics, scenicScoreMap);
        Integer budgetLevel = inferBudgetLevel(scenics, scenicScoreMap);
        String preferredSeason = inferPreferredSeason(scenics, scenicScoreMap);
        String interestKeywords = inferInterestKeywords(userId, scenics, scenicScoreMap);

        upsertProfile(userId, travelStyle, budgetLevel, preferredSeason, interestKeywords);
    }

    @Override
    public String getCachedSummary(Long userId) {
        if (userId == null) {
            return null;
        }
        Object value = redisUtils.get(buildSummaryCacheKey(userId));
        return value == null ? null : value.toString();
    }

    @Override
    public void asyncRefreshSummary(Long userId, PortraitSummaryContext context) {
        if (userId == null || context == null || !isLlmAvailable()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                String summary = generateSummary(userId, context);
                if (summary != null && !summary.isBlank()) {
                    redisUtils.set(buildSummaryCacheKey(userId), summary, SUMMARY_TTL);
                }
            } catch (Exception ex) {
                log.warn("生成画像摘要失败 userId={}", userId, ex);
            }
        });
    }

    private Map<Long, Double> aggregateScenicScores(Long userId) {
        List<UserBrowseHistory> recentBrowseList = userBrowseHistoryMapper.selectList(
                Wrappers.<UserBrowseHistory>lambdaQuery()
                        .eq(UserBrowseHistory::getUserId, userId)
                        .isNotNull(UserBrowseHistory::getScenicSpotId)
                        .orderByDesc(UserBrowseHistory::getCreateTime)
                        .orderByDesc(UserBrowseHistory::getId)
                        .last("LIMIT " + RECENT_BROWSE_LIMIT));
        Map<Long, Long> browseCount = recentBrowseList.stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getScenicSpotId, Collectors.counting()));

        List<UserFavorite> favoriteList = userFavoriteMapper.selectList(
                Wrappers.<UserFavorite>lambdaQuery()
                        .eq(UserFavorite::getUserId, userId)
                        .isNotNull(UserFavorite::getScenicSpotId));
        Map<Long, Long> favoriteCount = favoriteList.stream()
                .collect(Collectors.groupingBy(UserFavorite::getScenicSpotId, Collectors.counting()));

        List<UserReview> reviewList = userReviewMapper.selectList(
                Wrappers.<UserReview>lambdaQuery()
                        .eq(UserReview::getUserId, userId)
                        .isNotNull(UserReview::getScenicSpotId));
        Map<Long, Long> reviewCount = reviewList.stream()
                .collect(Collectors.groupingBy(UserReview::getScenicSpotId, Collectors.counting()));

        Map<Long, Double> scoreMap = new HashMap<>();
        browseCount.forEach((id, c) -> scoreMap.merge(id, c * BROWSE_WEIGHT, Double::sum));
        favoriteCount.forEach((id, c) -> scoreMap.merge(id, c * FAVORITE_WEIGHT, Double::sum));
        reviewCount.forEach((id, c) -> scoreMap.merge(id, c * REVIEW_WEIGHT, Double::sum));
        return scoreMap;
    }

    private List<ScenicSpot> loadScenics(Collection<Long> scenicIds) {
        if (scenicIds == null || scenicIds.isEmpty()) {
            return List.of();
        }
        return scenicSpotMapper.selectBatchIds(scenicIds);
    }

    /**
     * 把景点 category 加权汇总，再按映射规则归类到旅行风格标签。
     */
    private String inferTravelStyle(List<ScenicSpot> scenics, Map<Long, Double> scoreMap) {
        if (scenics.isEmpty()) {
            return null;
        }
        Map<String, Double> styleScore = new HashMap<>();
        for (ScenicSpot scenic : scenics) {
            Double score = scoreMap.getOrDefault(scenic.getId(), 0D);
            if (score <= 0D) {
                continue;
            }
            String style = mapCategoryToStyle(scenic.getCategory());
            if (style != null) {
                styleScore.merge(style, score, Double::sum);
            }
        }
        return styleScore.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String mapCategoryToStyle(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return switch (category) {
            case "自然风光" -> "自然探索型";
            case "历史古迹", "文化场馆", "古镇水乡" -> "人文历史型";
            case "主题乐园" -> "亲子休闲型";
            case "城市游览" -> "都市漫游型";
            case "美食街区" -> "美食打卡型";
            default -> "综合体验型";
        };
    }

    /**
     * 用景点门票价的加权平均映射预算档位。
     */
    private Integer inferBudgetLevel(List<ScenicSpot> scenics, Map<Long, Double> scoreMap) {
        if (scenics.isEmpty()) {
            return null;
        }
        double weightedSum = 0D;
        double weightTotal = 0D;
        for (ScenicSpot scenic : scenics) {
            BigDecimal price = scenic.getTicketPrice();
            if (price == null) {
                continue;
            }
            double score = scoreMap.getOrDefault(scenic.getId(), 0D);
            if (score <= 0D) {
                continue;
            }
            weightedSum += price.doubleValue() * score;
            weightTotal += score;
        }
        if (weightTotal <= 0D) {
            return null;
        }
        double avg = weightedSum / weightTotal;
        if (avg <= 50D) {
            return 1;
        }
        if (avg <= 150D) {
            return 2;
        }
        return 3;
    }

    /**
     * 取景点 best_season 的加权众数；只识别四个标准季节关键词。
     */
    private String inferPreferredSeason(List<ScenicSpot> scenics, Map<Long, Double> scoreMap) {
        if (scenics.isEmpty()) {
            return null;
        }
        Map<String, Double> seasonScore = new LinkedHashMap<>();
        for (ScenicSpot scenic : scenics) {
            String season = extractSeason(scenic.getBestSeason());
            if (season == null) {
                continue;
            }
            double score = scoreMap.getOrDefault(scenic.getId(), 0D);
            if (score <= 0D) {
                continue;
            }
            seasonScore.merge(season, score, Double::sum);
        }
        return seasonScore.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String extractSeason(String bestSeason) {
        if (bestSeason == null || bestSeason.isBlank()) {
            return null;
        }
        if (bestSeason.contains("春")) {
            return "春季";
        }
        if (bestSeason.contains("夏")) {
            return "夏季";
        }
        if (bestSeason.contains("秋")) {
            return "秋季";
        }
        if (bestSeason.contains("冬")) {
            return "冬季";
        }
        return null;
    }

    /**
     * 取 Top 标签名作为兴趣关键词，存为 JSON 数组字符串；同时合并用户手动设置的偏好标签优先级。
     */
    private String inferInterestKeywords(Long userId, List<ScenicSpot> scenics, Map<Long, Double> scoreMap) {
        Map<Long, Double> tagScore = new HashMap<>();
        if (!scenics.isEmpty()) {
            List<Long> scenicIds = scenics.stream().map(ScenicSpot::getId).toList();
            List<ScenicSpotTag> bindings = scenicSpotTagMapper.selectByScenicSpotIds(scenicIds);
            if (bindings != null) {
                for (ScenicSpotTag bind : bindings) {
                    if (bind.getScenicSpotId() == null || bind.getTagId() == null) {
                        continue;
                    }
                    double score = scoreMap.getOrDefault(bind.getScenicSpotId(), 0D);
                    if (score <= 0D) {
                        continue;
                    }
                    tagScore.merge(bind.getTagId(), score, Double::sum);
                }
            }
        }

        List<UserPreferenceTag> prefTags = userPreferenceTagMapper.selectList(
                Wrappers.<UserPreferenceTag>lambdaQuery().eq(UserPreferenceTag::getUserId, userId));
        for (UserPreferenceTag pref : prefTags) {
            if (pref.getTagId() != null) {
                tagScore.merge(pref.getTagId(), 2D, Double::sum);
            }
        }

        if (tagScore.isEmpty()) {
            return null;
        }
        List<Long> topIds = tagScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(INTEREST_KEYWORDS_TOP_N)
                .map(Map.Entry::getKey)
                .toList();
        List<Tag> tags = tagMapper.selectBatchIds(topIds);
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        Map<Long, String> nameMap = tags.stream()
                .filter(t -> t.getName() != null && !t.getName().isBlank())
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));
        List<String> names = topIds.stream()
                .map(nameMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (names.isEmpty()) {
            return null;
        }
        return JsonUtils.toJson(names);
    }

    private void upsertProfile(Long userId, String travelStyle, Integer budgetLevel,
                               String preferredSeason, String interestKeywords) {
        UserProfile existing = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setTravelStyle(travelStyle);
            profile.setBudgetLevel(budgetLevel);
            profile.setPreferredSeason(preferredSeason);
            profile.setInterestKeywords(interestKeywords);
            profile.setProfileVersion(1);
            profile.setLastAnalyzedAt(now);
            userProfileMapper.insert(profile);
            return;
        }
        UserProfile update = new UserProfile();
        update.setId(existing.getId());
        update.setTravelStyle(travelStyle);
        update.setBudgetLevel(budgetLevel);
        update.setPreferredSeason(preferredSeason);
        update.setInterestKeywords(interestKeywords);
        update.setProfileVersion((existing.getProfileVersion() == null ? 0 : existing.getProfileVersion()) + 1);
        update.setLastAnalyzedAt(now);
        userProfileMapper.updateById(update);
    }

    private boolean isLlmAvailable() {
        return !Boolean.FALSE.equals(llmProperties.getEnabled())
                && llmProperties.getApiKey() != null
                && !llmProperties.getApiKey().isBlank();
    }

    private String generateSummary(Long userId, PortraitSummaryContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfPresent(payload, "travelStyle", context.getTravelStyle());
        putIfPresent(payload, "budgetLevel", context.getBudgetLabel());
        putIfPresent(payload, "preferredSeason", context.getPreferredSeason());
        putIfPresent(payload, "location", context.getLocation());
        if (context.getPreferredTags() != null && !context.getPreferredTags().isEmpty()) {
            payload.put("preferredTags", context.getPreferredTags());
        }
        if (context.getRecentPreferences() != null && !context.getRecentPreferences().isEmpty()) {
            payload.put("recentPreferences", context.getRecentPreferences());
        }
        if (payload.isEmpty()) {
            return null;
        }
        String userPrompt = "请基于以下用户画像数据，写一段 30~50 字的中文摘要：\n" + JsonUtils.toJson(payload);
        LlmRequest request = LlmRequest.builder()
                .userId(userId)
                .modelName(llmProperties.getModelName())
                .timeoutMs(llmProperties.getTimeoutMs())
                .messages(List.of(
                        LlmRequest.Message.builder().role("system").content(SUMMARY_SYSTEM_PROMPT).build(),
                        LlmRequest.Message.builder().role("user").content(userPrompt).build()))
                .build();
        LlmResponse response = llmGateway.generate(request);
        if (response == null || response.getContent() == null) {
            return null;
        }
        return sanitize(response.getContent());
    }

    private void putIfPresent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank() && !"未知".equals(value) && !"未知地区".equals(value)
                && !"待发掘".equals(value)) {
            map.put(key, value);
        }
    }

    private String sanitize(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:\\w+)?", "").replaceFirst("```$", "").trim();
        }
        trimmed = trimmed.replaceAll("\\s+", "");
        return trimmed.length() > 80 ? trimmed.substring(0, 80) : trimmed;
    }

    private String buildSummaryCacheKey(Long userId) {
        return SUMMARY_CACHE_PREFIX + userId;
    }
}
