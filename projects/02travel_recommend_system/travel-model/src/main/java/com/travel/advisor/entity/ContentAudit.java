package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容审核实体
 */
@Data
@TableName("content_audit")
public class ContentAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 内容类型（review/image/scenic/plan）
     */
    private String contentType;

    /**
     * 内容ID（如评论ID、图片ID等）
     */
    private Long contentId;

    /**
     * 内容快照（JSON）
     */
    private String contentSnapshot;

    /**
     * 提交审核的用户ID
     */
    private Long submitUserId;

    /**
     * 审核状态（0-待审核，1-审核通过，2-审核不通过，3-人工复审）
     */
    private Integer auditStatus;

    /**
     * 自动审核结果（JSON格式，包含敏感词、违规类型等信息）
     */
    private String autoAuditResult;

    /**
     * 自动审核得分（0-100分，分数越高表示内容越健康）
     */
    private BigDecimal autoAuditScore;

    /**
     * LLM 审核调用记录ID（关联 llm_call_log 表）
     */
    private Long llmCallLogId;

    /**
     * 人工审核的管理员ID
     */
    private Long auditorId;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 违规类型（JSON数组）
     */
    private String violationType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
