package com.travel.advisor.service.audit;

import com.travel.advisor.dto.audit.AuditActionDTO;

/**
 * 审核策略接口，每种内容类型（review/image/scenic/plan）实现对应策略。
 */
public interface AuditStrategy {

    /**
     * 执行审核操作：更新审核记录状态和关联业务对象状态。
     *
     * @param contentId   内容ID（如 reviewId、fileResourceId、scenicSpotId、planId）
     * @param auditStatus 审核状态（ContentAuditStatus）
     * @param action      操作类型："approve"、"reject"、"hide"
     * @param dto         审核操作DTO，包含备注等信息
     */
    void executeAudit(Long contentId, Integer auditStatus, String action, AuditActionDTO dto);

    /**
     * 审核后刷新关联数据（如景点评分、文件状态等）。
     *
     * @param contentId 内容ID
     */
    void refreshRelatedData(Long contentId);
}
