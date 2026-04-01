package com.campus.resourcesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_comment")
public class GoodsComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}
