package com.travel.advisor.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

/** 聊天会话 VO */
@Data
public class ChatConversationVO {

    private Long conversationId;

    /** 会话标题 */
    private String title;

    /**
     * 会话类型：1-智能客服 2-行程规划 3-景点咨询
     */
    private Integer conversationType;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 消耗的总 token 数量
     */
    private Integer totalTokens;

    /**
     * 会话状态：0-关闭 1-进行中
     */
    private Integer status;

    private LocalDateTime createdAt;

    /**
     * 最后一条消息的时间
     */
    private LocalDateTime updatedAt;
}
