package com.travel.advisor.service;

import com.travel.advisor.common.enums.ContentAuditStatus;
import com.travel.advisor.common.enums.UserReviewStatus;
import com.travel.advisor.dto.llm.LlmRequest;
import com.travel.advisor.dto.llm.LlmResponse;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.llm.LlmGateway;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentAutoAuditService {

    private static final BigDecimal AUTO_PASS_THRESHOLD = new BigDecimal("0.95");
    private static final int MAX_REMARK_LENGTH = 500;
    private static final int MAX_VIOLATION_TYPE_LENGTH = 100;

    private final LlmGateway llmGateway;
    private final ContentAuditMapper contentAuditMapper;
    private final UserReviewMapper userReviewMapper;

    @Async("auditExecutor")
    public void auditReviewAsync(Long auditId, Long reviewId, String content) {
        try {
            doAudit(auditId, reviewId, content);
        } catch (Exception e) {
            log.error("自动审核异常, auditId={}, reviewId={}", auditId, reviewId, e);
            markManualReview(auditId, reviewId, "自动审核异常: " + e.getMessage());
        }
    }

    public void doAudit(Long auditId, Long reviewId, String content) {
        LlmRequest request = buildAuditRequest(content);
        LlmResponse response = llmGateway.generate(request);

        AuditResult result = parseAuditResult(response.getContent());

        ContentAudit update = new ContentAudit();
        update.setId(auditId);
        update.setAutoAuditResult(JsonUtils.toJson(result));
        update.setAutoAuditScore(result.score());
        String violationJson = result.violationTypes() == null ? null : JsonUtils.toJson(result.violationTypes());
        update.setViolationType(truncate(violationJson, MAX_VIOLATION_TYPE_LENGTH));
        update.setAuditTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());

        if (result.pass() && result.score().compareTo(AUTO_PASS_THRESHOLD) >= 0) {
            update.setAuditStatus(ContentAuditStatus.APPROVED.getCode());
            update.setAuditRemark("LLM自动审核通过");
            contentAuditMapper.updateById(update);
            updateReviewStatus(reviewId, UserReviewStatus.APPROVED.getCode(), null);
            log.info("自动审核通过, auditId={}, reviewId={}, score={}", auditId, reviewId, result.score());
        } else {
            update.setAuditStatus(ContentAuditStatus.MANUAL_REVIEW.getCode());
            update.setAuditRemark(result.pass() ? "置信分不足，转人工复审" : "检测到违规内容，转人工复审");
            contentAuditMapper.updateById(update);
            updateReviewStatus(reviewId, UserReviewStatus.PENDING.getCode(), null);
            log.info("转人工复审, auditId={}, reviewId={}, pass={}, score={}", auditId, reviewId, result.pass(), result.score());
        }
    }

    private void markManualReview(Long auditId, Long reviewId, String remark) {
        try {
            ContentAudit update = new ContentAudit();
            update.setId(auditId);
            update.setAuditStatus(ContentAuditStatus.MANUAL_REVIEW.getCode());
            update.setAuditRemark(truncate(remark, MAX_REMARK_LENGTH));
            update.setUpdateTime(LocalDateTime.now());
            contentAuditMapper.updateById(update);
        } catch (Exception e) {
            log.error("标记人工复审失败, auditId={}", auditId, e);
        }
    }

    private void updateReviewStatus(Long reviewId, Integer status, String auditRemark) {
        UserReview update = new UserReview();
        update.setId(reviewId);
        update.setStatus(status);
        update.setAuditRemark(auditRemark);
        update.setUpdateTime(LocalDateTime.now());
        userReviewMapper.updateById(update);
    }

    private LlmRequest buildAuditRequest(String content) {
        String systemPrompt = """
                你是一个内容审核助手。请对用户提交的旅游点评内容进行审核，判断是否存在违规内容。

                审核标准：
                1. 不得包含色情、暴力、恐怖等违法违规内容
                2. 不得包含广告、垃圾信息、恶意引流
                3. 不得包含人身攻击、侮辱谩骂、歧视性言论
                4. 不得包含虚假信息、恶意差评（无实质内容的攻击性评价）
                5. 不得包含政治敏感内容
                6. 不得包含个人隐私信息（手机号、身份证号等）

                请以JSON格式返回审核结果：
                {
                  "pass": true/false,
                  "score": 0-100的整数（内容健康度，越高越健康），
                  "violationTypes": ["违规类型1", "违规类型2"],
                  "reason": "简要说明审核理由"
                }

                如果内容合规，pass为true，score应>=95，violationTypes为空数组。
                如果内容存在轻微问题但不确定，pass为true但score在60-94之间。
                如果内容明确违规，pass为false，score<60，并列出违规类型。""";

        String userPrompt = "请审核以下旅游点评内容：\n\n" + content;

        return LlmRequest.builder()
                .jsonMode(true)
                .timeoutMs(15000)
                .messages(List.of(
                        LlmRequest.Message.builder().role("system").content(systemPrompt).build(),
                        LlmRequest.Message.builder().role("user").content(userPrompt).build()
                ))
                .build();
    }

    @SuppressWarnings("unchecked")
    private AuditResult parseAuditResult(String llmContent) {
        try {
            Map<String, Object> map = JsonUtils.fromJson(llmContent, Map.class);
            boolean pass = Boolean.TRUE.equals(map.get("pass"));
            Number scoreNum = (Number) map.getOrDefault("score", 0);
            // LLM returns 0-100, DB column is decimal(5,4) so store as 0-1 scale
            BigDecimal score = BigDecimal.valueOf(scoreNum.doubleValue()).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            List<String> violationTypes = (List<String>) map.get("violationTypes");
            String reason = (String) map.get("reason");
            return new AuditResult(pass, score, violationTypes, reason);
        } catch (Exception e) {
            log.warn("解析LLM审核结果失败, content={}", llmContent, e);
            return new AuditResult(false, BigDecimal.ZERO, List.of("解析失败"), "LLM返回格式异常");
        }
    }

    private static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    private record AuditResult(boolean pass, BigDecimal score, List<String> violationTypes, String reason) {}
}
