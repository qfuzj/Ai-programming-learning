package com.travel.advisor.service.audit;

import com.travel.advisor.common.enums.FileResourceStatus;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.entity.FileResource;
import com.travel.advisor.mapper.FileResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 图片审核策略：处理 image 类型的审核，更新 file_resource 状态。
 */
@Component("imageAuditStrategy")
@RequiredArgsConstructor
public class ImageAuditStrategy implements AuditStrategy {

    private final FileResourceMapper fileResourceMapper;

    @Override
    public void executeAudit(Long contentId, String action, AuditActionDTO dto) {
        FileResource fileResource = fileResourceMapper.selectById(contentId);
        if (fileResource == null) {
            return;
        }
        FileResource updateFile = new FileResource();
        updateFile.setId(fileResource.getId());
        updateFile.setStatus(resolveContentStatus(action));
        updateFile.setUpdateTime(LocalDateTime.now());
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
