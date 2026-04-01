package com.campus.resourcesharing.query.admin;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminNoticePageQuery extends PageQuery {

    private String keyword;
    private Integer status;
}
