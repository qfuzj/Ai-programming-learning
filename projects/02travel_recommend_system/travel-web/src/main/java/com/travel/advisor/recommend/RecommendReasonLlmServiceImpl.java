package com.travel.advisor.recommend;

import com.travel.advisor.common.enums.LLMCallLogStatus;
import com.travel.advisor.domain.recommend.RankedRecommend;
import com.travel.advisor.dto.llm.LlmChatRequest;
import com.travel.advisor.dto.llm.LlmChatResponse;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.llm.LlmProperties;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.service.LlmCallLogService;
import com.travel.advisor.utils.JsonUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /**
     * 生成推荐理由的核心方法，接收用户ID、场景信息和候选景点列表，调用LLM生成推荐理由，并记录调用日志
     * 1. 构建LLM请求，整合场景信息和候选景点列表，形成清晰的指令，指导LLM生成针对每个候选景点的推荐理由
     * 2. 调用LLM获取推荐理由，并解析LLM返回的JSON结果，提取每个景点对应的推荐理由文本，形成景点ID到推荐理由的映射
     * 3. 记录LLM调用日志，保存请求和响应内容，以及调用结果状态，便于后续分析和优化推荐理由生成的效果
     * 4. 返回生成的推荐理由结果对象，包含景点ID到推荐理由的映射、是否使用LLM以及LLM调用日志ID等信息，供调用方使用
     */
    @Override
    public RecommendReasonResult generateReasons(Long userId, String scene, List<RankedRecommend> pageItems) {
        if (pageItems == null || pageItems.isEmpty()) {
            return emptyResult();
        }

        List<ScenicReasonCandidate> candidates = buildCandidates(pageItems);
        String userPrompt = buildUserPrompt(scene, candidates);
        LlmChatRequest request = LlmChatRequest.builder()
                .userId(userId)
                .modelName(llmProperties.getModelName())
                .jsonMode(true)
                .timeoutMs(llmProperties.getTimeoutMs())
                .messages(List.of(
                        LlmChatRequest.Message.builder()
                                .role("system")
                                .content(SYSTEM_PROMPT)
                                .build(),
                        LlmChatRequest.Message.builder()
                                .role("user")
                                .content(userPrompt)
                                .build()))
                .build();

        long start = System.currentTimeMillis();
        String requestMessages = JsonUtils.toJson(request.getMessages());
        try {
            LlmChatResponse response = llmGateway.chat(request);
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
            // 记录调用日志，标记为成功，并保存请求和响应内容，便于后续分析和优化
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
            // 记录调用日志，标记为失败，并捕获异常信息，便于后续分析和优化
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

    /**
     * 构建推荐理由生成的候选项列表，提取景点的基本信息和排名分数等特征，供 LLM 生成推荐理由时参考
     * 1. 从输入的 RankedRecommend 列表中提取景点 ID、名称、所属区域名称、类别、等级、评分、来源类型和排名分数等信息
     * 2. 为每个景点构建一个 ScenicReasonCandidate 对象，封装上述信息，形成候选项列表
     * 3. 返回构建好的候选项列表，供后续构建用户提示和生成推荐理由使用
     */
    private List<ScenicReasonCandidate> buildCandidates(List<RankedRecommend> pageItems) {
        Set<Long> regionIds = pageItems.stream()
                .map(item -> item.getScenicSpot().getRegionId())
                .filter(id -> id != null && id > 0)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, String> regionNameMap = new LinkedHashMap<>();
        if (!regionIds.isEmpty()) {
            regionMapper.selectBatchIds(regionIds).stream()
                    .forEach(region -> regionNameMap.put(region.getId(), region.getName()));
        }

        List<ScenicReasonCandidate> candidates = new ArrayList<>(pageItems.size());
        for (RankedRecommend pageItem : pageItems) {
            candidates.add(ScenicReasonCandidate.builder()
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
    private String buildUserPrompt(String scene, List<ScenicReasonCandidate> candidates) {
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
        for (RecommendReasonItem item : payload.getItems()) {
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

    /**
     * 解析异常信息，判断是否包含超时相关的关键词，如果包含则返回 TIMEOUT 状态，否则返回 FAILED 状态
      * 1. 从异常对象开始，循环检查当前异常及其所有原因链中的异常信息
      * 2. 将异常信息转换为小写，并检查是否包含 "timeout" 关键词，如果包含则认为是超时异常
      * 3. 如果在整个异常链中都没有找到超时相关的关键词，则默认认为是一般的失败异常
      * 4. 通过这种方式可以更准确地识别出由于超时导致的调用失败，便于后续针对超时问题进行优化和处理
     */
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

    /**
     * ScenicReasonCandidate 是推荐理由生成的候选项对象，包含景点的基本信息和排名分数等特征，供 LLM 生成推荐理由时参考
      * 1. scenicId、name、regionName、category、level、score 等字段描述了景点的基本信息和特征，帮助 LLM 理解每个候选景点的特点
      * 2. sourceTypes 字段表示该景点的推荐来源类型（如基于兴趣、基于行为等），提供更多维度的信息供 LLM 生成更个性化的推荐理由
      * 3. rankScore 字段表示该景点在当前推荐列表中的综合排名分数，帮助 LLM 理解该景点相对于其他候选项的重要程度
      * 4. 通过这个类可以将输入的候选景点列表转换为一个结构化的对象列表，便于构建用户提示并指导 LLM 生成针对每个候选景点的推荐理由
      * 5. 在构建用户提示时，可以将这个候选项列表以 JSON 格式传递给 LLM，帮助 LLM 理解每个候选景点的详细信息，从而生成更有针对性的推荐理由
      * 6. 这个类的设计可以根据实际需要进行扩展，比如添加更多的景点特征字段，或者添加一些辅助方法来格式化候选项信息
     */
    @Data
    @lombok.Builder
    private static class ScenicReasonCandidate {

        private Long scenicId;

        private String name;

        private String regionName;

        private String category;

        private String level;

        private Double score;

        /**
         * 推荐来源类型集合，表示该景点的推荐来源类型（如基于兴趣、基于行为等），提供更多维度的信息供 LLM 生成更个性化的推荐理由
          * 1. 这个字段是一个 Set，允许一个景点有多个推荐来源类型，反映了实际推荐系统中可能存在的多样化推荐来源
          * 2. 在构建用户提示时，可以将这个字段以 JSON 格式传递给 LLM，帮助 LLM 理解每个候选景点的推荐来源，从而生成更有针对性的推荐理由
          * 3. 这个字段的设计可以根据实际需要进行调整，比如改为 List 或者添加更多的细节信息来描述推荐来源
         */
        private Set<String> sourceTypes;

        private Double rankScore;
    }

    /**
     * RecommendReasonPayload 用于解析 LLM 返回的推荐理由 JSON，包含一个推荐理由项列表，每个项包含景点ID和对应的推荐理由文本
      * 1. items 列表包含多个 RecommendReasonItem 对象，每个对象对应一个景点的推荐理由
      * 2. RecommendReasonItem 包含 scenicId 和 reason 两个字段，分别表示景点ID和推荐理由文本
      * 3. 通过这个类可以将 LLM 返回的 JSON 字符串解析为 Java 对象，方便后续处理和使用
     */
    @Data
    private static class RecommendReasonPayload {

        private List<RecommendReasonItem> items;
    }

    @Data
    private static class RecommendReasonItem {

        private Long scenicId;

        private String reason;
    }
}
