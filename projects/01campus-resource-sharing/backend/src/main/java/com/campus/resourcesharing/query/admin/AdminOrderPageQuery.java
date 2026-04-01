package com.campus.resourcesharing.query.admin;

import com.campus.resourcesharing.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderPageQuery extends PageQuery {

    private String orderNo;
    private String status;
    private Long buyerId;
    private Long sellerId;
    private String buyerKeyword;
    private String sellerKeyword;
}
