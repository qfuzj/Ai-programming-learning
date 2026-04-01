package com.campus.resourcesharing.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReportVO {

    private Long id;
    private Long reporterId;
    private String reporterName;
    private String targetType;
    private Long targetId;
    private String reason;
    private String description;
    private String status;
    private String handleResult;
    private LocalDateTime createTime;
}
