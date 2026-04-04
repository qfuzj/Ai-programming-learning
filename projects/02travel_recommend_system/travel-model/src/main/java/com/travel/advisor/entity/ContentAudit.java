package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("content_audit")
public class ContentAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String contentType;

    private Long contentId;

    private String contentSnapshot;

    private Long submitUserId;

    private Integer auditStatus;

    private String autoAuditResult;

    private BigDecimal autoAuditScore;

    private Long llmCallLogId;

    private Long auditorId;

    private String auditRemark;

    private LocalDateTime auditTime;

    private String violationType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
