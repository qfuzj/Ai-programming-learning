package com.campus.resourcesharing.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderVO {

    private Long id;
    private String orderNo;
    private Long goodsId;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal dealPrice;
    private String status;
    private LocalDateTime createTime;
}
