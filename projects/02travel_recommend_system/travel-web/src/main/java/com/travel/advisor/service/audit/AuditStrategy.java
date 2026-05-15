package com.travel.advisor.service.audit;

import com.travel.advisor.dto.audit.AuditActionDTO;

/**
 * 审核策略接口，每种内容类型（review/image/scenic/plan）实现对应策略。
 */
public interface AuditStrategy {

    /**
     * 更新内容关联的业务对象状态（如 user_review、scenic_spot、file_resource、travel_plan）。
     * 审核记录本身（content_audit 表）的状态由 AuditService 统一更新。
     *
     * @param contentId 内容ID（如 reviewId、fileResourceId、scenicSpotId、planId）
     * @param action    操作类型："approve"、"reject"、"hide"
     * @param dto       审核操作DTO，包含备注等信息
     */
    void executeAudit(Long contentId, String action, AuditActionDTO dto);

    /**
     * 审核后刷新关联数据（如景点评分、文件状态等）。
     *
     * @param contentId 内容ID
     */
    void refreshRelatedData(Long contentId);
}
