package com.travel.advisor.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户发送消息DTO，包含用户输入的消息内容和可选的上下文信息（如景点ID），便于后端结合上下文生成更精准的回复
 */
@Data
public class ChatSendMessageDTO {

    /**
     * 消息内容，不能为空，前端需要对用户输入进行校验，确保发送的消息有效且符合预期格式，便于后端处理和生成回复
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 景点ID，作为上下文信息传递给后端，便于LLM结合景点信息生成更精准的回复
     */
    private Long contextScenicId;
}
