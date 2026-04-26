package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.enums.ContentAuditStatus;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.dto.audit.AuditQueryDTO;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.entity.User;
import com.travel.advisor.vo.audit.AuditVO;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.entity.FileResource;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.FileResourceMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.service.AuditService;
import com.travel.advisor.service.audit.AuditStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final Map<String, AuditStrategy> auditStrategyMap;
    private final ContentAuditMapper contentAuditMapper;
    private final UserReviewMapper userReviewMapper;
    private final UserMapper userMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final FileResourceMapper fileResourceMapper;

    @Override
    public PageResult<AuditVO> page(AuditQueryDTO dto) {
        LambdaQueryWrapper<ContentAudit> wrapper = new LambdaQueryWrapper<ContentAudit>()
                .eq(StringUtils.hasText(dto.getContentType()), ContentAudit::getContentType, dto.getContentType())
                .eq(dto.getAuditStatus() != null, ContentAudit::getAuditStatus, dto.getAuditStatus())
                .eq(dto.getContentId() != null, ContentAudit::getContentId, dto.getContentId())
                .eq(dto.getSubmitUserId() != null, ContentAudit::getSubmitUserId, dto.getSubmitUserId())
                .orderByDesc(ContentAudit::getCreateTime);

        Page<ContentAudit> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<ContentAudit> result = contentAuditMapper.selectPage(page, wrapper);

        // 批量加载点评相关数据，避免逐条 N+1 查询
        ReviewEnrichContext enrichContext = loadReviewEnrichContext(result.getRecords());

        return PageResult.<AuditVO>builder()
                .records(result.getRecords().stream()
                        .map(audit -> convertToVO(audit, enrichContext))
                        .collect(Collectors.toList()))
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public AuditVO getById(Long id) {
        ContentAudit audit = contentAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核记录不存在");
        }
        return convertToVO(audit, loadReviewEnrichContext(List.of(audit)));
    }

    private AuditVO convertToVO(ContentAudit audit, ReviewEnrichContext enrichContext) {
        AuditVO vo = BeanCopyUtils.copy(audit, AuditVO.class);
        if (StringUtils.hasText(audit.getContentSnapshot())) {
            try {
                vo.setSnapshot(JsonUtils.fromJson(audit.getContentSnapshot(), Object.class));
            } catch (Exception e) {
                // ignore
            }
        }
        if (StringUtils.hasText(audit.getAutoAuditResult())) {
            try {
                vo.setAutoAuditResult(JsonUtils.fromJson(audit.getAutoAuditResult(), Object.class));
            } catch (Exception e) {
                // ignore
            }
        }
        if (StringUtils.hasText(audit.getViolationType())) {
            try {
                vo.setViolationType(JsonUtils.fromJson(audit.getViolationType(), Object.class));
            } catch (Exception e) {
                // ignore
            }
        }
        enrichReviewSnapshot(vo, audit, enrichContext);
        return vo;
    }

    /**
     * 批量加载审核记录关联的点评、用户、景点、图片资源数据，作为 enrich 阶段的上下文，
     * 避免对每条审核记录单独查询造成 N+1。
     */
    private ReviewEnrichContext loadReviewEnrichContext(List<ContentAudit> audits) {
        if (audits == null || audits.isEmpty()) {
            return ReviewEnrichContext.EMPTY;
        }
        List<Long> reviewIds = audits.stream()
                .filter(a -> "review".equalsIgnoreCase(a.getContentType()))
                .map(ContentAudit::getContentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (reviewIds.isEmpty()) {
            return ReviewEnrichContext.EMPTY;
        }

        Map<Long, UserReview> reviewMap = userReviewMapper.selectBatchIds(reviewIds).stream()
                .collect(Collectors.toMap(UserReview::getId, java.util.function.Function.identity(), (a, b) -> a));

        List<Long> userIds = reviewMap.values().stream()
                .map(UserReview::getUserId).filter(Objects::nonNull).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, java.util.function.Function.identity(), (a, b) -> a));

        List<Long> scenicIds = reviewMap.values().stream()
                .map(UserReview::getScenicSpotId).filter(Objects::nonNull).distinct().toList();
        Map<Long, ScenicSpot> scenicMap = scenicIds.isEmpty()
                ? Collections.emptyMap()
                : scenicSpotMapper.selectBatchIds(scenicIds).stream()
                .collect(Collectors.toMap(ScenicSpot::getId, java.util.function.Function.identity(), (a, b) -> a));

        List<Long> allImageIds = reviewMap.values().stream()
                .flatMap(r -> parseImageIds(r.getImages()).stream())
                .distinct()
                .toList();
        Map<Long, String> imageUrlMap = allImageIds.isEmpty()
                ? Collections.emptyMap()
                : fileResourceMapper.selectBatchIds(allImageIds).stream()
                .filter(fr -> fr.getUrl() != null)
                .collect(Collectors.toMap(FileResource::getId, FileResource::getUrl, (a, b) -> a));

        return new ReviewEnrichContext(reviewMap, userMap, scenicMap, imageUrlMap);
    }

    private void enrichReviewSnapshot(AuditVO vo, ContentAudit audit, ReviewEnrichContext ctx) {
        if (!"review".equalsIgnoreCase(audit.getContentType())) {
            return;
        }

        UserReview review = ctx.reviewMap().get(audit.getContentId());
        if (review == null) {
            return;
        }

        User user = review.getUserId() == null ? null : ctx.userMap().get(review.getUserId());
        ScenicSpot scenicSpot = review.getScenicSpotId() == null ? null : ctx.scenicMap().get(review.getScenicSpotId());

        Map<String, Object> snapshotMap = new LinkedHashMap<>();
        if (vo.getSnapshot() instanceof Map<?, ?> rawMap) {
            rawMap.forEach((key, value) -> {
                if (key instanceof String stringKey) {
                    snapshotMap.put(stringKey, value);
                }
            });
        }

        snapshotMap.put("userId", review.getUserId());
        snapshotMap.put("username", user == null ? null : user.getUsername());
        snapshotMap.put("scenicId", review.getScenicSpotId());
        snapshotMap.put("scenicName", scenicSpot == null ? null : scenicSpot.getName());
        snapshotMap.put("rating", review.getRating());
        snapshotMap.put("content", review.getContent());

        List<Long> imageIds = parseImageIds(review.getImages());
        if (!imageIds.isEmpty()) {
            List<String> imageUrls = imageIds.stream()
                    .map(ctx.imageUrlMap()::get)
                    .filter(Objects::nonNull)
                    .toList();
            if (!imageUrls.isEmpty()) {
                snapshotMap.put("images", imageUrls);
            }
        }

        vo.setSnapshot(snapshotMap);
    }

    /**
     * Enrich 阶段使用的批量上下文：在调用前一次性查询，VO 转换时直接命中内存。
     */
    private record ReviewEnrichContext(
            Map<Long, UserReview> reviewMap,
            Map<Long, User> userMap,
            Map<Long, ScenicSpot> scenicMap,
            Map<Long, String> imageUrlMap
    ) {
        static final ReviewEnrichContext EMPTY = new ReviewEnrichContext(
                Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap());
    }

    private List<Long> parseImageIds(String images) {
        if (!StringUtils.hasText(images)) {
            return Collections.emptyList();
        }
        try {
            Long[] arr = JsonUtils.fromJson(images, Long[].class);
            return arr == null ? Collections.emptyList() : List.of(arr);
        } catch (Exception e) {
            // 静默降级会让脏数据难以排查，记录 warn 便于后续通过日志定位异常点评行
            log.warn("parseImageIds failed, images='{}', err={}", images, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(Long id, AuditActionDTO dto) {
        executeAuditWithStrategy(id, ContentAuditStatus.APPROVED.getCode(), "approve", dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reject(Long id, AuditActionDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        executeAuditWithStrategy(id, ContentAuditStatus.REJECTED.getCode(), "reject", dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void hide(Long id, AuditActionDTO dto) {
        executeAuditWithStrategy(id, ContentAuditStatus.APPROVED.getCode(), "hide", dto);
    }

    private void executeAuditWithStrategy(Long auditId, Integer auditStatus, String action, AuditActionDTO dto) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核记录不存在");
        }

        AuditStrategy strategy = getStrategy(audit.getContentType());
        strategy.executeAudit(audit.getContentId(), auditStatus, action, dto);
        strategy.refreshRelatedData(audit.getContentId());
    }

    private AuditStrategy getStrategy(String contentType) {
        AuditStrategy strategy = auditStrategyMap.get(contentType + "AuditStrategy");
        if (strategy == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的审核类型: " + contentType);
        }
        return strategy;
    }
}
