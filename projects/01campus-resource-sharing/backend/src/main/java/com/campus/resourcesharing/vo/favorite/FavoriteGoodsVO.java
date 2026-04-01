package com.campus.resourcesharing.vo.favorite;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteGoodsVO {

    private Long favoriteId;
    private LocalDateTime favoriteTime;

    private Long goodsId;
    private Long userId;
    private Long categoryId;
    private String title;
    private BigDecimal price;
    private String conditionLevel;
    private String mainImage;
    private String status;
    private Integer favoriteCount;
    private Integer viewCount;
    private LocalDateTime goodsCreateTime;
}
