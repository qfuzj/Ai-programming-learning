package com.travel.advisor.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 发送消息 DTO */
@Data
public class ChatSendMessageDTO {

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 上下文景点 ID */
    private Long contextScenicId;
}
