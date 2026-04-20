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

    /** LLM 回复消息 ID */
    private Long assistantMessageId;

    /** LLM 回复内容 */
    private String replyContent;

    /** 消耗 tokens */
    private Integer tokenUsage;

    /** 模型名称 */
    private String modelName;
}
