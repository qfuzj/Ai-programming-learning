package com.travel.advisor.dto.audit;

import com.travel.advisor.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditQueryDTO extends PageQuery {

    /**
     * 内容类型，如 （review/image/scenic/plan）
     */
    private String contentType;

    /**
     * 审核状态：0-待审核 1-通过 2-拒绝
     */
    private Integer auditStatus;

    /**
     * 被审核内容ID
     */
    private Long contentId;

    /**
     * 提交用户ID
     */
    private Long submitUserId;
}
