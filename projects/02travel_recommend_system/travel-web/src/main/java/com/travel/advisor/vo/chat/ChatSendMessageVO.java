package com.travel.advisor.vo.chat;

import lombok.Data;

/**
 * 发送消息接口响应DTO
 */
@Data
public class ChatSendMessageVO {

    /**
     * 用户输入消息ID
     */
    private Long userMessageId;

    /**
     * LLM 回复消息ID，便于前端展示和后续消息关联
     */
    private Long assistantMessageId;

    /**
     * LLM 回复内容，包含文本回复和可能的推荐景点列表等信息，便于前端展示和用户交互
     */
    private String replyContent;

    /**
     * 消耗的 token 数量，记录本次消息交互中用户输入和 LLM 回复所使用的 token 数量，便于前端展示和后续分析优化
     */
    private Integer tokenUsage;

    /**
     * 模型名称，记录本次消息交互使用的 LLM 模型名称，便于前端展示和后续分析优化
     */
    private String modelName;
}
