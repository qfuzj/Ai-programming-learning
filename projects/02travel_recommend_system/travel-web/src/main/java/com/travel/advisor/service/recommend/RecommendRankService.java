package com.travel.advisor.service.recommend;

import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.domain.recommend.RecallCandidate;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐结果排序服务
 */
@Service
@RequiredArgsConstructor
public class RecommendRankService {

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
        Map<Long, BigDecimal> scoreMap = new HashMap<>();
        Map<Long, Set<String>> sourceMap = new HashMap<>();

        for (RecallCandidate candidate : candidates) {
            Long id = candidate.getScenicId();
            // 过滤：ID为空 或 需要排除的景点ID
            if (id == null || id.equals(excludeScenicId)) {
                continue;
            }

            // 分数累加：如果同一景点被多个召回策略命中，则将它们的基础分数相加，得到一个综合得分。
            BigDecimal score = Optional.ofNullable(candidate.getBaseScore()).orElse(BigDecimal.ZERO);
            scoreMap.merge(id, score, BigDecimal::add);

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
                                .rankScore(scoreMap.getOrDefault(item.getId(), BigDecimal.ZERO))
                                .sourceTypes(sourceMap.getOrDefault(item.getId(), Collections.emptySet()))
                                .build()
                ).sorted(Comparator.comparing(RankedRecommend::getRankScore, Comparator.reverseOrder()))
                .limit(limit).toList();

        // 5. LLM 精排扩展点：后续可在这里根据上下文和候选结果进行二次重排。
        // TODO: 可以在此处将ranked列表交给大模型，结合用户画像（UserContext）进行语义化打分修正
        return reRankByLLM(ranked);
    }

    /**
     * LLM 精排扩展点：后续可在这里根据上下文和候选结果进行二次重排。
     *
     * @param currentRanked - 当前基于规则打分和过滤后的推荐结果列表，包含景点信息、综合得分和来源类型集合。
     * @return 经过LLM精排后的推荐结果列表，理论上应该在原有基础上进行微调排序，以提升个性化和相关性。
     */
    private List<RankedRecommend> reRankByLLM(List<RankedRecommend> currentRanked) {
        return currentRanked;
    }
}
