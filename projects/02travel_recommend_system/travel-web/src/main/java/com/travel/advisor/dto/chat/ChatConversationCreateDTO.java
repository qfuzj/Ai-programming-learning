package com.travel.advisor.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatConversationCreateDTO {

    @NotBlank(message = "会话标题不能为空")
    private String title;

    @NotBlank(message = "会话类型不能为空")
    private Integer conversationType;
}
