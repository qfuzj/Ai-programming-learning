package com.campus.resourcesharing.query.admin;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReportPageQuery extends PageQuery {

    private String targetType;
    private String status;
    private Long reporterId;
    private String reporterKeyword;
}
