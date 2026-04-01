package com.campus.resourcesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_image")
public class GoodsImage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private String imageUrl;
    private Integer sort;
    private LocalDateTime createTime;
}
