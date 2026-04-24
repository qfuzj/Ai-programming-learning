package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.ScenicSpotStatus;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.ScenicSpotMapper;
import com.travel.advisor.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 景点审核策略：处理 scenic 类型的审核，更新 scenic_spot 状态。
 */
@Component("scenicAuditStrategy")
@RequiredArgsConstructor
public class ScenicAuditStrategy implements AuditStrategy {

    private final ContentAuditMapper contentAuditMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    @Override
    public void executeAudit(Long contentId, Integer auditStatus, String action, AuditActionDTO dto) {
        Long auditorId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        String reason = dto == null ? null : dto.getReason();

        // 根据 action 决定 scenic_spot 的状态
        Integer contentStatus = resolveContentStatus(action);

        // 更新审核记录
        ContentAudit updateAudit = new ContentAudit();
        updateAudit.setId(contentId);
        updateAudit.setAuditStatus(auditStatus);
        updateAudit.setAuditRemark(reason);
        updateAudit.setAuditorId(auditorId);
        updateAudit.setAuditTime(now);
        updateAudit.setUpdateTime(now);
        contentAuditMapper.updateById(updateAudit);

        // 更新景点状态
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(contentId);
        if (scenicSpot == null) {
            return;
        }
        ScenicSpot updateScenic = new ScenicSpot();
        updateScenic.setId(scenicSpot.getId());
        updateScenic.setStatus(contentStatus);
        updateScenic.setUpdateTime(now);
        scenicSpotMapper.updateById(updateScenic);
    }

    @Override
    public void refreshRelatedData(Long contentId) {
        // 景点审核无需刷新关联数据
    }

    private Integer resolveContentStatus(String action) {
        return switch (action) {
            case "approve" -> ScenicSpotStatus.ACTIVE.getCode();
            case "reject" -> ScenicSpotStatus.INACTIVE.getCode();
            default -> throw new IllegalArgumentException("未知的操作类型: " + action);
        };
    }
}
