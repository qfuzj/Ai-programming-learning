package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.TravelPlanStatus;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.TravelPlan;
import com.travel.advisor.mapper.TravelPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 行程审核策略：处理 plan 类型的审核，更新 travel_plan 状态。
 */
@Component("planAuditStrategy")
@RequiredArgsConstructor
public class PlanAuditStrategy implements AuditStrategy {

    private final TravelPlanMapper travelPlanMapper;

    @Override
    public void executeAudit(Long contentId, String action, AuditActionDTO dto) {
        TravelPlan travelPlan = travelPlanMapper.selectById(contentId);
        if (travelPlan == null) {
            return;
        }
        TravelPlan updatePlan = new TravelPlan();
        updatePlan.setId(travelPlan.getId());
        updatePlan.setStatus(resolveContentStatus(action));
        updatePlan.setUpdateTime(LocalDateTime.now());
        travelPlanMapper.updateById(updatePlan);
    }

    @Override
    public void refreshRelatedData(Long contentId) {
        // 行程审核无需刷新关联数据
    }

    private Integer resolveContentStatus(String action) {
        return switch (action) {
            case "approve" -> TravelPlanStatus.PUBLISHED.getCode();
            case "reject" -> 0; // 回到草稿状态
            default -> throw new IllegalArgumentException("未知的操作类型: " + action);
        };
    }
}
