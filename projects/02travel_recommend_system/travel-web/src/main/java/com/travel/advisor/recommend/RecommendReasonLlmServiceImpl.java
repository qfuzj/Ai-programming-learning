package com.travel.advisor.recommend;

import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.service.LlmCallLogService;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendReasonLlmServiceImpl implements RecommendReasonLlmService {

    private static final String CALL_TYPE = "recommend";
    private static final Duration REASON_CACHE_TTL = Duration.ofHours(24);
    private static final String SYSTEM_PROMPT = """
            你是旅游推荐理由生成助手。
            你会根据候选景点信息生成简洁、自然、面向当前用户的中文推荐理由。
            你必须只输出 JSON，不要输出 Markdown、解释或额外文本。
            返回格式必须是：
            {"items":[{"scenicId":1,"reason":"推荐理由"}]}
            每条 reason 控制在 18 到 40 个中文字符，避免空话和模板化重复。
            """;

    private final LlmGateway llmGateway;
    private final LlmProperties llmProperties;
    private final LlmCallLogService llmCallLogService;
    private final RegionMapper regionMapper;
    private final RedisUtils redisUtils;

    /** 生成推荐理由：构建 prompt → 调用 LLM → 解析结果 → 记录日志 */
    @Override
    public RecommendReasonResult generateReasons(Long userId, String scene, List<RankedRecommend> pageItems) {
        if (pageItems == null || pageItems.isEmpty()) {
            return emptyResult();
        }
        if (!isLlmAvailable()) {
            return emptyResult();
        }

        List<RecommendReasonPromptCandidate> candidates = buildCandidates(pageItems);
        String cacheKey = buildReasonCacheKey(scene, candidates);
        String cachedValue = redisUtils.get(cacheKey);
        if (cachedValue != null && !cachedValue.isBlank()) {
            RecommendReasonCachePayload payload = JsonUtils.fromJson(cachedValue, RecommendReasonCachePayload.class);
            if (payload != null && payload.getReasons() != null && !payload.getReasons().isEmpty()) {
                return RecommendReasonResult.builder()
                        .reasons(payload.getReasons())
                        .llmUsed(true)
                        .llmCallLogId(null)
                        .build();
            }
        }

        String userPrompt = buildUserPrompt(scene, candidates);
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
            // 解析 LLM 返回的推荐理由 JSON，提取每个景点对应的推荐理由文本，形成景点ID到推荐理由的映射
            Map<Long, String> reasons = parseReasons(response.getContent());
            if (reasons.isEmpty()) {
                Long callLogId = llmCallLogService.saveCallLog(
                        userId,
                        CALL_TYPE,
                        userPrompt,
                        requestMessages,
                        response,
                        LLMCallLogStatus.FAILED.getCode(),
                        "LLM 返回结果无法解析为推荐理由 JSON",
                        (int) (System.currentTimeMillis() - start));
                return RecommendReasonResult.builder()
                        .reasons(Collections.emptyMap())
                        .llmUsed(false)
                        .llmCallLogId(callLogId)
                        .build();
            }
            cacheReasons(cacheKey, reasons);
            // 记录成功日志
            Long callLogId = llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    response,
                    LLMCallLogStatus.SUCCESS.getCode(),
                    null,
                    (int) (System.currentTimeMillis() - start));
            return RecommendReasonResult.builder()
                    .reasons(reasons)
                    .llmUsed(true)
                    .llmCallLogId(callLogId)
                    .build();
        } catch (Exception ex) {
            // 记录失败日志
            Long callLogId = llmCallLogService.saveCallLog(
                    userId,
                    CALL_TYPE,
                    userPrompt,
                    requestMessages,
                    null,
                    resolveStatus(ex).getCode(),
                    ex.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            return RecommendReasonResult.builder()
                    .reasons(Collections.emptyMap())
                    .llmUsed(false)
                    .llmCallLogId(callLogId)
                    .build();
        }
    }

    private String buildReasonCacheKey(String scene, List<RecommendReasonPromptCandidate> candidates) {
        List<Long> scenicIds = candidates.stream()
                .map(RecommendReasonPromptCandidate::getScenicId)
                .toList();
        String raw = scene + ":" + JsonUtils.toJson(scenicIds);
        String digest = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return "recommend:reason:" + digest;
    }

    private boolean isLlmAvailable() {
        return !Boolean.FALSE.equals(llmProperties.getEnabled())
                && llmProperties.getApiKey() != null
                && !llmProperties.getApiKey().isBlank();
    }

    private void cacheReasons(String cacheKey, Map<Long, String> reasons) {
        RecommendReasonCachePayload payload = new RecommendReasonCachePayload();
        payload.setReasons(reasons);
        redisUtils.set(cacheKey, JsonUtils.toJson(payload), REASON_CACHE_TTL);
    }

    /**
     * 构建推荐理由生成的候选项列表，提取景点的基本信息和排名分数等特征，供 LLM 生成推荐理由时参考
     * 1. 从输入的 RankedRecommend 列表中提取景点 ID、名称、所属区域名称、类别、等级、评分、来源类型和排名分数等信息
     * 2. 为每个景点构建一个 RecommendReasonPromptCandidate 对象，封装上述信息，形成候选项列表
     * 3. 返回构建好的候选项列表，供后续构建用户提示和生成推荐理由使用
     */
    private List<RecommendReasonPromptCandidate> buildCandidates(List<RankedRecommend> pageItems) {
        Set<Long> regionIds = pageItems.stream()
                .map(item -> item.getScenicSpot().getRegionId())
                .filter(id -> id != null && id > 0)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, String> regionNameMap = new LinkedHashMap<>();
        if (!regionIds.isEmpty()) {
            regionMapper.selectBatchIds(regionIds).stream()
                    .forEach(region -> regionNameMap.put(region.getId(), region.getName()));
        }

        List<RecommendReasonPromptCandidate> candidates = new ArrayList<>(pageItems.size());
        for (RankedRecommend pageItem : pageItems) {
            candidates.add(RecommendReasonPromptCandidate.builder()
                    .scenicId(pageItem.getScenicSpot().getId())
                    .name(pageItem.getScenicSpot().getName())
                    .regionName(regionNameMap.get(pageItem.getScenicSpot().getRegionId()))
                    .category(pageItem.getScenicSpot().getCategory())
                    .level(pageItem.getScenicSpot().getLevel())
                    .score(pageItem.getScenicSpot().getScore())
                    .sourceTypes(pageItem.getSourceTypes())
                    .rankScore(pageItem.getRankScore())
                    .build());
        }
        return candidates;
    }

    /**
     * 构建用户提示，整合场景信息和候选景点列表，形成清晰的指令，指导 LLM 生成针对每个候选景点的推荐理由
     */
    private String buildUserPrompt(String scene, List<RecommendReasonPromptCandidate> candidates) {
        return """
                请根据以下候选景点信息生成推荐理由，并以 JSON 返回。
                场景：%s
                输出 JSON 中的 scenicId 必须与输入一致，每个候选景点都尽量给出一条理由。
                候选景点列表：
                %s
                """.formatted(scene, JsonUtils.toJson(candidates));
    }

    /**
     * 解析 LLM 返回的推荐理由 JSON，提取每个景点对应的推荐理由文本，形成景点ID到推荐理由的映射
     * 1. 验证输入内容是否为空或仅包含空白字符，如果是则返回空映射
     * 2. 去除可能存在的 Markdown 代码块标记，提取纯 JSON 内容
     * 3. 将 JSON 字符串解析为 RecommendReasonPayload 对象，捕获解析异常并返回空映射
     */
    private Map<Long, String> parseReasons(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyMap();
        }
        String normalized = stripJsonFence(content).trim();
        RecommendReasonPayload payload;
        try {
            payload = JsonUtils.fromJson(normalized, RecommendReasonPayload.class);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
        if (payload == null || payload.getItems() == null || payload.getItems().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> result = new LinkedHashMap<>();
        for (RecommendReasonPayload.RecommendReasonItem item : payload.getItems()) {
            if (item == null || item.getScenicId() == null || item.getReason() == null) {
                continue;
            }
            String reason = item.getReason().trim();
            if (!reason.isBlank()) {
                result.put(item.getScenicId(), reason);
            }
        }
        return result;
    }

    private String stripJsonFence(String content) {
        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?", "");
            normalized = normalized.replaceFirst("```$", "");
        }
        return normalized.trim();
    }

    /** 判断异常是否为超时：遍历 cause 链检查 timeout 关键词 */
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

    private RecommendReasonResult emptyResult() {
        return RecommendReasonResult.builder()
                .reasons(Collections.emptyMap())
                .llmUsed(false)
                .llmCallLogId(null)
                .build();
    }

    @lombok.Data
    private static class RecommendReasonCachePayload {

        private Map<Long, String> reasons;
    }
}
