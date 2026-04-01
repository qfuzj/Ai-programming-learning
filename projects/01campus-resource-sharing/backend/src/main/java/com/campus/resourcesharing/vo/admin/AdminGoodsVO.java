package com.campus.resourcesharing.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminGoodsVO {

    private Long id;
    private Long userId;
    private String userName;
    private Long categoryId;
    private String title;
    private BigDecimal price;
    private String status;
    private LocalDateTime createTime;
}
