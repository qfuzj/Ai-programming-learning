package com.campus.resourcesharing.query.admin;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminGoodsPageQuery extends PageQuery {

    private String keyword;
    private String status;
    private Long categoryId;
    private Long userId;
    private String userKeyword;
}
