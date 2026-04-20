package com.travel.advisor.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

/** 聊天消息 VO */
@Data
public class ChatMessageVO {

    private Long messageId;

    /** 角色：user / assistant / system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 内容类型：1 文本 2 图片 3 卡片 */
    private Integer contentType;

    /** 消耗 tokens */
    private Integer tokensUsed;

    /** 关联 LLM 调用日志 ID */
    private Long llmCallLogId;

    private LocalDateTime createdAt;
}
