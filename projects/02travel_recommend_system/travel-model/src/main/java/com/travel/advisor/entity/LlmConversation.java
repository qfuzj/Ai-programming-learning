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

    /** 会话标题 */
    private String title;

    /**
     * 会话类型：1-智能客服 2-行程规划 3-景点咨询
     */
    private Integer conversationType;

    /** 上下文数据（JSON） */
    private String contextData;

    /** 消息数量 */
    private Integer messageCount;

    /** 消耗总 tokens */
    private Integer totalTokens;

    /** 最后消息时间 */
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
