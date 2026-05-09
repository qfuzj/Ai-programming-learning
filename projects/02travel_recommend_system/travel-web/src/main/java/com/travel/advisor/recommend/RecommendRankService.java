package com.travel.advisor.recommend;

import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.domain.recommend.RecallCandidate;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐结果排序服务
 */
@Service
@RequiredArgsConstructor
public class RecommendRankService {

    private static final Map<String, Double> SOURCE_WEIGHTS = Map.of(
            "TAG", 1.35,
            "GEO", 0.65,
            "HOT", 0.35,
            "SIMILAR_TAG", 1.15);
    private static final double MULTI_SOURCE_BONUS = 0.15;
    private static final double MAX_MULTI_SOURCE_BONUS = 0.3;
    private static final double MAX_SCORE_BONUS = 0.1;

    private final ScenicSpotService scenicSpotService;

    /**
     * 对召回的候选景点进行打分聚合、过滤并排序，返回最终的推荐结果列表。
     *
     * @param candidates      - 来自不同召回策略的候选景点列表，包含景点ID、基础分数和来源类型等信息。
     * @param excludeScenicId - 可选的景点ID，若不为null，则在排序时排除该景点（例如当前正在查看的景点）。
     * @param limit           - 最终返回的推荐结果数量上限。
     * @return 排序后的推荐结果列表，每个结果包含景点信息、综合得分和来源类型集合。
     */
    public List<RankedRecommend> rank(List<RecallCandidate> candidates, Long excludeScenicId, int limit) {
        // 1. 前置校验：如果候选列表为空，直接返回空结果。
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 数据聚合：将多路召回的相同景点分数累加，并合并来源类型。
        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, Set<String>> sourceMap = new HashMap<>();

        for (RecallCandidate candidate : candidates) {
            Long id = candidate.getScenicId();
            // 过滤：ID为空 或 需要排除的景点ID
            if (id == null || id.equals(excludeScenicId)) {
                continue;
            }

            Double score = Optional.ofNullable(candidate.getBaseScore()).orElse(0.0);
            double sourceWeight = SOURCE_WEIGHTS.getOrDefault(candidate.getSourceType(), 0.7);
            scoreMap.merge(id, score * sourceWeight, Double::sum);

            // 来源合并
            if (candidate.getSourceType() != null) {
                sourceMap.computeIfAbsent(id, k -> new HashSet<>()).add(candidate.getSourceType());
            }
        }

        if (scoreMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 批量状态校验：从数据库获取状态为1的景点信息
        Map<Long, ScenicSpot> scenicMap = scenicSpotService.listByIdsWithStatus(scoreMap.keySet(), 1).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        // 4. 排序和封装：根据综合得分对景点进行降序排序，并封装成最终的推荐结果对象列表。
        List<RankedRecommend> ranked = scenicMap.values().stream()
                .map(item ->
                        RankedRecommend.builder()
                                .scenicSpot(item)
                                .rankScore(buildFinalScore(
                                        scoreMap.getOrDefault(item.getId(), 0.0),
                                        sourceMap.getOrDefault(item.getId(), Collections.emptySet()),
                                        item.getScore()))
                                .sourceTypes(sourceMap.getOrDefault(item.getId(), Collections.emptySet()))
                                .build()
                ).sorted(Comparator.comparing(RankedRecommend::getRankScore, Comparator.reverseOrder()))
                .limit(limit).toList();

        // 5. LLM 精排扩展点：如需基于用户画像 / 上下文做语义化重排，在此处接入；当前未实现。
        return ranked;
    }

    private double buildFinalScore(double weightedScore, Set<String> sources, Double scenicScore) {
        int sourceCount = sources == null ? 0 : sources.size();
        double sourceBonus = sourceCount <= 1
                ? 0.0
                : Math.min(MAX_MULTI_SOURCE_BONUS, (sourceCount - 1) * MULTI_SOURCE_BONUS);
        double qualityBonus = scenicScore == null ? 0.0 : Math.min(MAX_SCORE_BONUS, scenicScore / 10.0 * MAX_SCORE_BONUS);
        return weightedScore + sourceBonus + qualityBonus;
    }
}
