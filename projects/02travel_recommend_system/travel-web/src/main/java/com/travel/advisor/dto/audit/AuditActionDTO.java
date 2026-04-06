package com.travel.advisor.dto.audit;

import lombok.Data;

@Data
public class AuditActionDTO {

    /**
     * 审核备注；拒绝时建议必填
     */
    private String reason;
}
