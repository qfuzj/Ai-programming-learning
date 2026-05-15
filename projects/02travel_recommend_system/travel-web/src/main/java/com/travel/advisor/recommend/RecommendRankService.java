package com.travel.advisor.recommend;

import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.domain.recommend.RecallCandidate;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.ScenicSpotTag;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.mapper.ScenicSpotTagMapper;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.service.LlmCallLogService;
import com.travel.advisor.service.ScenicSpotService;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.vo.user.UserProfilePortraitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐结果排序服务
 */
@Service
@RequiredArgsConstructor
public class RecommendRankService {

    private static final String CALL_TYPE = "recommend_rank";
    private static final int LLM_RERANK_MAX_ITEMS = 20;
    private static final String SYSTEM_PROMPT = """
            你是旅游推荐精排助手。
            你需要根据用户画像、候选景点信息和当前场景，对候选景点进行重新排序。
            只输出 JSON，不要输出 Markdown、解释或额外文本。
            返回格式必须是：
            {"orderedScenicIds":[1,2,3]}
            规则：
            1. 只能使用候选列表中的 scenicId，不能新增、删除或重复。
            2. 优先匹配用户画像中的旅行风格、预算水平、常去地区、偏好标签和近期偏好。
            3. 兼顾候选景点的基础分数、来源类型、季节、设施和体验，但不要机械照搬当前排序。
            4. 如果信息不足，请尽量保持候选列表原有顺序。
            """;

    private static final Map<String, Double> SOURCE_WEIGHTS = Map.of(
            "TAG", 1.35,
            "GEO", 0.65,
            "HOT", 0.35,
            "SIMILAR_TAG", 1.15);
    private static final double MULTI_SOURCE_BONUS = 0.15;
    private static final double MAX_MULTI_SOURCE_BONUS = 0.3;
    private static final double MAX_SCORE_BONUS = 0.1;

    private final ScenicSpotService scenicSpotService;
    private final LlmGateway llmGateway;
    private final LlmProperties llmProperties;
    private final LlmCallLogService llmCallLogService;
    private final RegionMapper regionMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final TagMapper tagMapper;

    /**
     * 对召回的候选景点进行打分聚合、过滤并排序，返回最终的推荐结果列表。
     *
     * @param userId          当前用户ID
     * @param scene           推荐场景，如 home / scenic-similar
     * @param portrait        当前用户画像
     * @param candidates      来自不同召回策略的候选景点列表
     * @param excludeScenicId 可选的景点ID，若不为null，则在排序时排除该景点
     * @param limit           最终返回的推荐结果数量上限
     * @return 排序后的推荐结果列表
     */
    public List<RankedRecommend> rank(Long userId,
                                      String scene,
                                      UserProfilePortraitVO portrait,
                                      List<RecallCandidate> candidates,
                                      Long excludeScenicId,
                                      int limit) {
        if (candidates == null || candidates.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, Set<String>> sourceMap = new HashMap<>();

        for (RecallCandidate candidate : candidates) {
            Long id = candidate.getScenicId();
            if (id == null || id.equals(excludeScenicId)) {
                continue;
            }

            Double score = Optional.ofNullable(candidate.getBaseScore()).orElse(0.0);
            double sourceWeight = SOURCE_WEIGHTS.getOrDefault(candidate.getSourceType(), 0.7);
            scoreMap.merge(id, score * sourceWeight, Double::sum);

            if (candidate.getSourceType() != null) {
                sourceMap.computeIfAbsent(id, k -> new HashSet<>()).add(candidate.getSourceType());
            }
        }

        if (scoreMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ScenicSpot> scenicMap = scenicSpotService.listByIdsWithStatus(scoreMap.keySet(), 1).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<RankedRecommend> ranked = scenicMap.values().stream()
                .map(item -> RankedRecommend.builder()
                        .scenicSpot(item)
                        .rankScore(buildFinalScore(
                                scoreMap.getOrDefault(item.getId(), 0.0),
                                sourceMap.getOrDefault(item.getId(), Collections.emptySet()),
                                item.getScore()))
                        .sourceTypes(sourceMap.getOrDefault(item.getId(), Collections.emptySet()))
                        .build())
                .sorted(Comparator.comparing(RankedRecommend::getRankScore, Comparator.reverseOrder()))
                .limit(limit)
                .toList();

        return applyLlmRerank(userId, scene, portrait, ranked);
    }

    private double buildFinalScore(double weightedScore, Set<String> sources, Double scenicScore) {
        int sourceCount = sources == null ? 0 : sources.size();
        double sourceBonus = sourceCount <= 1
                ? 0.0
                : Math.min(MAX_MULTI_SOURCE_BONUS, (sourceCount - 1) * MULTI_SOURCE_BONUS);
        double qualityBonus = scenicScore == null
                ? 0.0
                : Math.min(MAX_SCORE_BONUS, scenicScore / 10.0 * MAX_SCORE_BONUS);
        return weightedScore + sourceBonus + qualityBonus;
    }

    private List<RankedRecommend> applyLlmRerank(Long userId,
                                                 String scene,
                                                 UserProfilePortraitVO portrait,
                                                 List<RankedRecommend> ranked) {
        if (!isLlmAvailable() || ranked == null || ranked.size() <= 1) {
            return ranked;
        }

        int candidateLimit = Math.min(ranked.size(), LLM_RERANK_MAX_ITEMS);
        List<RankedRecommend> focusItems = new ArrayList<>(ranked.subList(0, candidateLimit));
        List<RecommendRankPromptCandidate> promptCandidates = buildPromptCandidates(focusItems);
        String userPrompt = buildUserPrompt(scene, portrait, promptCandidates);

        LlmRequest request = LlmRequest.builder()
                .userId(userId)
                .modelName(llmProperties.getModelName())
                .jsonMode(true)
                .timeoutMs(llmProperties.getTimeoutMs())
                .messages(List.of(
                        LlmRequest.Message.builder()
                                .role("system")
                                .content(SYSTEM_PROMPT)
                                .build(),
                        LlmRequest.Message.builder()
                                .role("user")
                                .content(userPrompt)
                                .build()))
                .build();

        long start = System.currentTimeMillis();
        String requestMessages = JsonUtils.toJson(request.getMessages());
        try {
            LlmResponse response = llmGateway.generate(request);
            List<Long> orderedIds = parseOrderedIds(response == null ? null : response.getContent(), promptCandidates);
            if (orderedIds.isEmpty()) {
                llmCallLogService.saveCallLog(
                        userId,
                        CALL_TYPE,
                        userPrompt,
                        requestMessages,
                        response,
                        LLMCallLogStatus.FAILED.getCode(),
                        "LLM 返回结果无法解析为景点排序 JSON",
                        (int) (System.currentTimeMillis() - start));
                return ranked;
            }

            List<RankedRecommend> reordered = reorderItems(focusItems, orderedIds);
            if (candidateLimit < ranked.size()) {
                reordered.addAll(ranked.subList(candidateLimit, ranked.size()));
            }

            llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    response,
                    LLMCallLogStatus.SUCCESS.getCode(),
                    null,
                    (int) (System.currentTimeMillis() - start));
            return reordered;
        } catch (Exception ex) {
            llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    null,
                    resolveStatus(ex).getCode(),
                    ex.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            return ranked;
        }
    }

    private boolean isLlmAvailable() {
        return !Boolean.FALSE.equals(llmProperties.getEnabled())
                && StringUtils.hasText(llmProperties.getApiKey());
    }

    private List<RecommendRankPromptCandidate> buildPromptCandidates(List<RankedRecommend> ranked) {
        if (ranked == null || ranked.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> scenicIds = ranked.stream()
                .map(item -> item.getScenicSpot().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> regionNameMap = loadRegionNameMap(ranked);
        Map<Long, List<String>> scenicTagsMap = loadScenicTags(scenicIds);

        List<RecommendRankPromptCandidate> candidates = new ArrayList<>(ranked.size());
        for (RankedRecommend item : ranked) {
            ScenicSpot scenicSpot = item.getScenicSpot();
            candidates.add(RecommendRankPromptCandidate.builder()
                    .scenicId(scenicSpot.getId())
                    .name(safeText(scenicSpot.getName(), 40))
                    .regionName(regionNameMap.get(scenicSpot.getRegionId()))
                    .category(safeText(scenicSpot.getCategory(), 20))
                    .level(safeText(scenicSpot.getLevel(), 10))
                    .ticketPrice(scenicSpot.getTicketPrice())
                    .bestSeason(safeText(scenicSpot.getBestSeason(), 30))
                    .suggestedHours(safeText(scenicSpot.getSuggestedHours(), 20))
                    .description(safeText(scenicSpot.getDescription(), 60))
                    .tags(scenicTagsMap.getOrDefault(scenicSpot.getId(), Collections.emptyList()))
                    .sourceTypes(item.getSourceTypes())
                    .rankScore(item.getRankScore())
                    .build());
        }
        return candidates;
    }

    private Map<Long, String> loadRegionNameMap(List<RankedRecommend> ranked) {
        Set<Long> regionIds = ranked.stream()
                .map(item -> item.getScenicSpot().getRegionId())
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return regionMapper.selectBatchIds(regionIds).stream()
                .filter(region -> region.getId() != null && StringUtils.hasText(region.getName()))
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getName(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, List<String>> loadScenicTags(Set<Long> scenicIds) {
        if (scenicIds == null || scenicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ScenicSpotTag> scenicSpotTags = scenicSpotTagMapper.selectByScenicSpotIds(new ArrayList<>(scenicIds));
        if (scenicSpotTags == null || scenicSpotTags.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = scenicSpotTags.stream()
                .map(ScenicSpotTag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> tag.getId() != null && StringUtils.hasText(tag.getName()))
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));

        Map<Long, List<String>> scenicTagsMap = new LinkedHashMap<>();
        for (ScenicSpotTag item : scenicSpotTags) {
            if (item.getScenicSpotId() == null || item.getTagId() == null) {
                continue;
            }
            String tagName = tagNameMap.get(item.getTagId());
            if (!StringUtils.hasText(tagName)) {
                continue;
            }
            scenicTagsMap.computeIfAbsent(item.getScenicSpotId(), key -> new ArrayList<>());
            List<String> tagNames = scenicTagsMap.get(item.getScenicSpotId());
            if (!tagNames.contains(tagName)) {
                tagNames.add(tagName);
            }
        }
        scenicTagsMap.replaceAll((key, value) -> value.size() > 5 ? new ArrayList<>(value.subList(0, 5)) : value);
        return scenicTagsMap;
    }

    private String buildUserPrompt(String scene,
                                   UserProfilePortraitVO portrait,
                                   List<RecommendRankPromptCandidate> candidates) {
        return """
                请根据用户画像和候选景点信息重新排序，输出最适合当前用户的景点顺序。
                场景：%s
                用户画像：%s
                候选景点列表：
                %s
                只返回 JSON，格式必须为：
                {"orderedScenicIds":[1,2,3]}
                """.formatted(
                safeText(scene, 30),
                buildPortraitPrompt(portrait),
                JsonUtils.toJson(candidates));
    }

    private String buildPortraitPrompt(UserProfilePortraitVO portrait) {
        if (portrait == null) {
            return "暂无用户画像，请依据候选景点自身特征进行排序。";
        }
        Map<String, Object> profile = new LinkedHashMap<>();
        putIfUseful(profile, "travelStyle", portrait.getTravelStyle(), "待发掘");
        putIfUseful(profile, "budgetLevel", portrait.getBudgetLevel(), "未知");
        putIfUseful(profile, "summary", portrait.getSummary(), "暂无足够数据生成画像");
        putIfUseful(profile, "location", portrait.getLocation(), "未知地区");
        if (portrait.getPreferredTags() != null && !portrait.getPreferredTags().isEmpty()) {
            profile.put("preferredTags", portrait.getPreferredTags());
        }
        if (portrait.getRecentPreferences() != null && !portrait.getRecentPreferences().isEmpty()) {
            profile.put("recentPreferences", portrait.getRecentPreferences());
        }
        return profile.isEmpty()
                ? "暂无用户画像，请依据候选景点自身特征进行排序。"
                : JsonUtils.toJson(profile);
    }

    private void putIfUseful(Map<String, Object> profile, String key, String value, String ignoredValue) {
        if (StringUtils.hasText(value) && !value.equals(ignoredValue)) {
            profile.put(key, value);
        }
    }

    private List<Long> parseOrderedIds(String content, List<RecommendRankPromptCandidate> candidates) {
        if (!StringUtils.hasText(content) || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = stripJsonFence(content);
        RecommendRankPayload payload;
        try {
            payload = JsonUtils.fromJson(normalized, RecommendRankPayload.class);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        if (payload == null || payload.getOrderedScenicIds() == null || payload.getOrderedScenicIds().isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> allowedIds = candidates.stream()
                .map(RecommendRankPromptCandidate::getScenicId)
                .collect(Collectors.toSet());
        List<Long> orderedIds = new ArrayList<>();
        for (Long scenicId : payload.getOrderedScenicIds()) {
            if (scenicId == null || !allowedIds.contains(scenicId) || orderedIds.contains(scenicId)) {
                continue;
            }
            orderedIds.add(scenicId);
        }
        return orderedIds;
    }

    private List<RankedRecommend> reorderItems(List<RankedRecommend> focusItems, List<Long> orderedIds) {
        if (focusItems == null || focusItems.isEmpty() || orderedIds == null || orderedIds.isEmpty()) {
            return focusItems == null ? Collections.emptyList() : new ArrayList<>(focusItems);
        }

        Map<Long, RankedRecommend> itemMap = focusItems.stream()
                .filter(item -> item.getScenicSpot() != null && item.getScenicSpot().getId() != null)
                .collect(Collectors.toMap(item -> item.getScenicSpot().getId(), item -> item, (a, b) -> a, LinkedHashMap::new));

        List<RankedRecommend> reordered = new ArrayList<>(focusItems.size());
        for (Long scenicId : orderedIds) {
            RankedRecommend item = itemMap.remove(scenicId);
            if (item != null) {
                reordered.add(item);
            }
        }
        reordered.addAll(itemMap.values());
        return reordered;
    }

    private String stripJsonFence(String content) {
        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?", "");
            normalized = normalized.replaceFirst("```$", "");
        }
        return normalized.trim();
    }

    private String safeText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private LLMCallLogStatus resolveStatus(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("timeout")) {
                return LLMCallLogStatus.TIMEOUT;
            }
            current = current.getCause();
        }
        return LLMCallLogStatus.FAILED;
    }
}
