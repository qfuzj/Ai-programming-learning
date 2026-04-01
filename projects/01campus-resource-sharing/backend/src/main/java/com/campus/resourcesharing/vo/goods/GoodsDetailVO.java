package com.campus.resourcesharing.vo.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsDetailVO {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String conditionLevel;
    private String contactInfo;
    private String tradeLocation;
    private String mainImage;
    private Integer viewCount;
    private Integer favoriteCount;
    private String status;
    private List<String> imageList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Boolean owner;
    private Boolean favorited;
}
