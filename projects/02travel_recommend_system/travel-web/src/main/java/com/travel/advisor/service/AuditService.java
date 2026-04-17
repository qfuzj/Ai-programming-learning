package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.dto.audit.AuditQueryDTO;
import com.travel.advisor.vo.audit.AuditVO;

public interface AuditService {

    PageResult<AuditVO> page(AuditQueryDTO dto);

    AuditVO getById(Long id);

    void approve(Long id, AuditActionDTO dto);

    void reject(Long id, AuditActionDTO dto);

    void hide(Long id, AuditActionDTO dto);
}
