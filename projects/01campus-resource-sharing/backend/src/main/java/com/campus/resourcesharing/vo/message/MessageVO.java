package com.campus.resourcesharing.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private Long goodsId;
    private String goodsTitle;
    private String goodsMainImage;

    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;

    private String content;
    private String messageType;
    private Integer isRead;
    private Boolean mine;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
