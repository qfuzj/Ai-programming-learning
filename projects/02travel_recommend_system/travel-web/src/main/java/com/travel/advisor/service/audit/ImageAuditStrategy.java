package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.FileResourceStatus;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.entity.FileResource;
import com.travel.advisor.mapper.ContentAuditMapper;
import com.travel.advisor.mapper.FileResourceMapper;
import com.travel.advisor.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 图片审核策略：处理 image 类型的审核，更新 file_resource 状态。
 */
@Component("imageAuditStrategy")
@RequiredArgsConstructor
public class ImageAuditStrategy implements AuditStrategy {

    private final ContentAuditMapper contentAuditMapper;
    private final FileResourceMapper fileResourceMapper;

    @Override
    public void executeAudit(Long contentId, Integer auditStatus, String action, AuditActionDTO dto) {
        Long auditorId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        String reason = dto == null ? null : dto.getReason();

        // 根据 action 决定 file_resource 的状态
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

        // 更新文件资源状态
        FileResource fileResource = fileResourceMapper.selectById(contentId);
        if (fileResource == null) {
            return;
        }
        FileResource updateFile = new FileResource();
        updateFile.setId(fileResource.getId());
        updateFile.setStatus(contentStatus);
        updateFile.setUpdateTime(now);
        fileResourceMapper.updateById(updateFile);
    }

    @Override
    public void refreshRelatedData(Long contentId) {
        // 图片审核无需刷新关联数据
    }

    private Integer resolveContentStatus(String action) {
        return switch (action) {
            case "approve" -> FileResourceStatus.USED.getCode();
            case "reject" -> FileResourceStatus.DELETED.getCode();
            default -> throw new IllegalArgumentException("未知的操作类型: " + action);
        };
    }
}
