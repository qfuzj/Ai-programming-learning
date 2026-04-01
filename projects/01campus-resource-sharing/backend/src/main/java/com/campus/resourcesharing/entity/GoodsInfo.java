package com.campus.resourcesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods_info")
public class GoodsInfo {

    @TableId(type = IdType.AUTO)
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
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
