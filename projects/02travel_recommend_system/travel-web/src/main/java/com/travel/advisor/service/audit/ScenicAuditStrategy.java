package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.ScenicSpotStatus;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.mapper.ScenicSpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 景点审核策略：处理 scenic 类型的审核，更新 scenic_spot 状态。
 */
@Component("scenicAuditStrategy")
@RequiredArgsConstructor
public class ScenicAuditStrategy implements AuditStrategy {

    private final ScenicSpotMapper scenicSpotMapper;

    @Override
    public void executeAudit(Long contentId, String action, AuditActionDTO dto) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(contentId);
        if (scenicSpot == null) {
            return;
        }
        ScenicSpot updateScenic = new ScenicSpot();
        updateScenic.setId(scenicSpot.getId());
        updateScenic.setStatus(resolveContentStatus(action));
        updateScenic.setUpdateTime(LocalDateTime.now());
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
