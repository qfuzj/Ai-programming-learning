package com.campus.resourcesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_message")
public class GoodsMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long senderId;
    private Long receiverId;
    private Long orderId;
    private String content;
    private String messageType;
    private Integer isRead;
    private LocalDateTime createTime;
}
