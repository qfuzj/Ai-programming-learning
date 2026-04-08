package com.travel.advisor.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM 消息VO类，用于前端展示和交互，包含消息的基本信息和相关联的LLM调用日志ID，便于前端根据需要展示消息内容和调用日志详情
 */
@Data
public class ChatMessageVO {

    private Long id;

    /**
     * 消息角色，如 "user"、"assistant"、"system" 等，便于前端根据角色区分消息来源和展示样式
     */
    private String role;

    /**
     * 消息内容，可以是文本、JSON 或其他格式，根据 contentType 字段区分，便于前端根据内容类型选择合适的展示方式
     */
    private String content;

    /**
     * 内容类型，1 表示文本，2 图片，便于前端根据内容类型选择合适的展示方式
     */
    private Integer contentType;

    /**
     * 消耗token数量，记录每条消息的token使用情况，便于前端展示消息的成本信息和后续分析优化
     */
    private Integer tokensUsed;

    /**
     * 关联的LLM调用日志ID，便于前端根据需要展示消息内容和调用日志详情，提供更丰富的上下文信息和用户体验
     */
    private Long llmCallLogId;

    private LocalDateTime createTime;
}
