package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.dto.audit.AuditQueryDTO;
import com.travel.advisor.entity.ContentAudit;

public interface AuditService {

    PageResult<ContentAudit> page(AuditQueryDTO dto);

    void approve(Long id, AuditActionDTO dto);

    void reject(Long id, AuditActionDTO dto);

    void hide(Long id, AuditActionDTO dto);
}
