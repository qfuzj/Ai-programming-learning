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

    /**
     * 会话ID，便于LLM识别当前聊天上下文，结合用户历史消息生成更精准的回复
     */
    private Long conversationId;

    /**
     * 模型名称，指定使用哪个LLM模型进行回复生成，不同模型在理解和生成能力上可能有所差异，选择合适的模型可以提升回复质量
     */
    private String modelName;

    /**
     * 是否开启JSON模式，开启后LLM将严格按照预定义的JSON格式输出回复，便于系统解析和处理，适用于需要结构化数据的场景
     */
    private Boolean jsonMode;

    /**
     * 请求超时时间，单位毫秒，控制LLM回复的最长等待时间，避免长时间阻塞系统资源，同时也能提升用户体验，合理设置超时时间可以平衡回复质量和响应速度
     */
    private Integer timeoutMs;

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
