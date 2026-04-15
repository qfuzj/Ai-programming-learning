package com.travel.advisor.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建聊天会话DTO
 */
@Data
public class ChatConversationCreateDTO {

    @NotBlank(message = "会话标题不能为空")
    private String title;

    /**
     * 会话类型：1-智能客服 2-行程规划 3-景点咨询
     */
    @NotNull(message = "会话类型不能为空")
    private Integer conversationType;
}
