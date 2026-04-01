package com.campus.resourcesharing.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCommentVO {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}
