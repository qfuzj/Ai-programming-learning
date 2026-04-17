package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.enums.ContentAuditStatus;
import com.travel.advisor.common.enums.UserReviewStatus;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.dto.audit.AuditQueryDTO;
import com.travel.advisor.entity.ContentAudit;

import com.travel.advisor.vo.audit.AuditVO;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.entity.UserReview;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.UserReviewMapper;
import com.travel.advisor.service.AuditService;
import com.travel.advisor.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    // 目前仅支持点评审核，后续可扩展其他内容类型（如图片、攻略等），建议使用枚举或常量管理内容类型
    private static final String REVIEW_CONTENT_TYPE = "review";

    private final ContentAuditMapper contentAuditMapper;
    private final UserReviewMapper userReviewMapper;

    /**
     * 获取管理员审核分页列表
     */
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

        return PageResult.<AuditVO>builder()
                .records(result.getRecords().stream().map(this::convertToVO)
                        .collect(java.util.stream.Collectors.toList()))
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    /**
     * 获取管理员审核详情
     * 
     * @param id 审核记录ID
     * @return 审核记录详情VO对象
     */
    @Override
    public AuditVO getById(Long id) {
        ContentAudit audit = contentAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核记录不存在");
        }
        return convertToVO(audit);
    }

    private AuditVO convertToVO(ContentAudit audit) {
        AuditVO vo = BeanCopyUtils.copy(audit, AuditVO.class);
        if (StringUtils.hasText(audit.getContentSnapshot())) {
            try {
                vo.setSnapshot(JsonUtils.fromJson(audit.getContentSnapshot(), Object.class));
            } catch (Exception e) {
                // ignore or log
            }
        }
        if (StringUtils.hasText(audit.getAutoAuditResult())) {
            try {
                vo.setAutoAuditResult(JsonUtils.fromJson(audit.getAutoAuditResult(), Object.class));
            } catch (Exception e) {
            }
        }
        if (StringUtils.hasText(audit.getViolationType())) {
            try {
                vo.setViolationType(JsonUtils.fromJson(audit.getViolationType(), Object.class));
            } catch (Exception e) {
            }
        }
        return vo;
    }

    /**
     * 批准审核
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(Long id, AuditActionDTO dto) {
        updateAuditAndReview(id, ContentAuditStatus.APPROVED.getCode(), UserReviewStatus.APPROVED.getCode(), dto);
    }

    /**
     * 拒绝审核
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reject(Long id, AuditActionDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        updateAuditAndReview(id, ContentAuditStatus.REJECTED.getCode(), UserReviewStatus.REJECTED.getCode(), dto);
    }

    /**
     * 隐藏审核
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void hide(Long id, AuditActionDTO dto) {
        // content_audit 无“隐藏”状态，按“已审核通过”记录审核动作，实际隐藏落在 user_review.status=3
        updateAuditAndReview(id, ContentAuditStatus.APPROVED.getCode(), UserReviewStatus.HIDDEN.getCode(), dto);
    }

    /**
     * 更新审核记录和用户点评状态
     * 
     * @param auditId      审核记录ID
     * @param auditStatus  审核状态：见 ContentAuditStatusEnum
     * @param reviewStatus 点评状态：见 UserReviewStatusEnum
     * @param dto          审核操作DTO，包含审核备注等信息
     */
    private void updateAuditAndReview(Long auditId,
            Integer auditStatus,
            Integer reviewStatus,
            AuditActionDTO dto) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核记录不存在");
        }
        if (!REVIEW_CONTENT_TYPE.equalsIgnoreCase(audit.getContentType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前记录不是点评审核");
        }

        UserReview userReview = userReviewMapper.selectById(audit.getContentId());
        if (userReview == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "点评不存在");
        }

        Long auditorId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        String reason = dto == null ? null : dto.getReason();

        ContentAudit updateAudit = new ContentAudit();
        updateAudit.setId(auditId);
        updateAudit.setAuditStatus(auditStatus);
        updateAudit.setAuditRemark(reason);
        updateAudit.setAuditorId(auditorId);
        updateAudit.setAuditTime(now);
        updateAudit.setUpdateTime(now);
        contentAuditMapper.updateById(updateAudit);

        UserReview updateReview = new UserReview();
        updateReview.setId(userReview.getId());
        updateReview.setStatus(reviewStatus);
        updateReview.setAuditRemark(reason);
        updateReview.setUpdateTime(now);
        userReviewMapper.updateById(updateReview);
    }
}
