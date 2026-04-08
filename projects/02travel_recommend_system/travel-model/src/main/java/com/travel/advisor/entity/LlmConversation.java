package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM 会话表实体
 */
@Data
@TableName("llm_conversation")
public class LlmConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 会话标题，用户可自定义，便于区分不同的会话
     */
    private String title;

    /**
     * 会话类型：1-智能客服 2-行程规划 3-景点咨询
     */
    private Integer conversationType;

    /**
     * 会话上下文数据，存储为 JSON 字符串，包含用户输入、AI 回复、当前景点等信息
     */
    private String contextData;

    /**
     * 消息数量，记录会话中用户和 AI 之间的消息总数，便于统计和分析
     */
    private Integer messageCount;

    /**
     * 消耗的总 token 数量，记录会话中用户输入和 AI 回复所使用的 token 数量，便于统计和分析
     */
    private Integer totalTokens;

    /**
     * 最后一条消息的时间，记录会话中最后一条消息的时间，便于统计和分析
     */
    private LocalDateTime lastMessageAt;

    /**
     * 会话状态：0-关闭 1-进行中
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
