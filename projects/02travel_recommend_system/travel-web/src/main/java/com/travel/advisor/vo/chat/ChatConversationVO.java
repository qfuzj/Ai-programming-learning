package com.travel.advisor.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM 会话VO类，用于前端展示会话列表和会话详情，包含会话的基本信息和统计数据，便于用户快速浏览和管理自己的会话记录
 */
@Data
public class ChatConversationVO {

    private Long id;

    /**
     * 会话标题，用户可自定义，便于区分不同的会话
     */
    private String title;

    /**
     * 会话类型：1-智能客服 2-行程规划 3-景点咨询
     */
    private String conversationType;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 消耗的总 token 数量
     */
    private Integer totalTokens;

    /**
     * 最后一条消息的时间
     */
    private LocalDateTime lastMessageAt;

    /**
     * 会话状态：0-关闭 1-进行中
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
