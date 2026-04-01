package com.campus.resourcesharing.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Long goodsId;
    private String goodsTitle;
    private String goodsMainImage;

    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;

    private BigDecimal dealPrice;
    private String status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
}
