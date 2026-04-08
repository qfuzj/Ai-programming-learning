package com.travel.advisor.dto.llm;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * LLM聊天请求对象，包含用户ID、会话ID、模型名称和消息列表等信息，用于向LLM发送聊天请求。
 */
@Data
@Builder
public class LlmChatRequest {

    private Long userId;

    /**
     * 会话ID，便于LLM识别当前聊天上下文，结合用户历史消息生成更精准的回复
     */
    private Long conversationId;

    private String modelName;

    /**
     * 消息列表，包含用户输入的消息和LLM生成的回复，按照顺序排列，便于LLM理解聊天上下文并生成连贯的回复
     */
    private List<Message> messages;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
