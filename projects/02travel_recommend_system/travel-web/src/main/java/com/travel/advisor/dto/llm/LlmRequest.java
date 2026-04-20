package com.travel.advisor.dto.llm;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 通用 LLM 请求对象。
 */
@Data
@Builder
public class LlmRequest {

    private Long userId;

    /** 会话 ID */
    private Long conversationId;

    /** 模型名称 */
    private String modelName;

    /** 是否 JSON 模式 */
    private Boolean jsonMode;

    /** 超时（ms） */
    private Integer timeoutMs;

    /** 消息列表 */
    private List<Message> messages;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
