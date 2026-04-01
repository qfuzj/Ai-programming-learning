package com.campus.resourcesharing.query.admin;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCommentPageQuery extends PageQuery {

    private Long goodsId;
    private Long fromUserId;
    private Long toUserId;
    private String fromUserKeyword;
    private String toUserKeyword;
    private Integer score;
}
