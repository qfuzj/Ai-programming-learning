package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.audit.AuditActionDTO;
import com.travel.advisor.dto.audit.AuditQueryDTO;
import com.travel.advisor.entity.ContentAudit;
import com.travel.advisor.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public Result<PageResult<ContentAudit>> page(AuditQueryDTO dto) {
        return Result.success(auditService.page(dto));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @Valid @RequestBody(required = false) AuditActionDTO dto) {
        auditService.approve(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @Valid @RequestBody AuditActionDTO dto) {
        auditService.reject(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/hide")
    public Result<Void> hide(@PathVariable Long id,
                             @Valid @RequestBody(required = false) AuditActionDTO dto) {
        auditService.hide(id, dto);
        return Result.success();
    }
}
