package com.travel.advisor.vo.audit;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对应-审核记录VO
 */
@Data
public class AuditVO {

    /** 主键ID */
    private Long id;
    /** 内容类型 (review/image/scenic/plan等) */
    private String contentType;
    /** 内容ID */
    private Long contentId;

    /** 快照数据（解析后的对象，以便前端接收JSON，而非字符串） */
    private Object snapshot;

    /** 提交用户ID */
    private Long submitUserId;
    /** 审核状态 (0=待审核, 1=通过, 2=拒绝, 3=隐藏) */
    private Integer auditStatus;

    /** 自动审核结果 */
    private Object autoAuditResult;
    /** 自动审核得分 */
    private BigDecimal autoAuditScore;

    /** LLM调用日志ID */
    private Long llmCallLogId;
    /** 审核员ID */
    private Long auditorId;
    /** 审核备注 */
    private String auditRemark;
    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 违规类型 */
    private Object violationType;

    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
