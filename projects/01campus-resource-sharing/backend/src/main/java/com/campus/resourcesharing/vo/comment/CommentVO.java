package com.campus.resourcesharing.vo.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Integer score;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
