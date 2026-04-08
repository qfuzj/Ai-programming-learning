package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM 消息实体类
 */
@Data
@TableName("llm_message")
public class LlmMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID，关联到 LLM 会话表
     */
    private Long conversationId;

    /**
     * 消息角色，如 "user"、"assistant"、"system" 等
     */
    private String role;

    /**
     * 消息内容，可以是文本、JSON 或其他格式，根据 contentType 字段区分
     */
    private String content;

    /**
     * 内容类型，1 表示文本，2 图片
     */
    private Integer contentType;

    /**
     * 附加数据，JSON格式，如推荐景点列表
     */
    private String extraData;

    /**
     * 消耗token数量，记录每条消息的token使用情况，便于后续分析和优化
     */
    private Integer tokensUsed;

    /**
     * 关联的LLM调用日志ID，便于追踪每条消息对应的LLM调用记录
     */
    private Long llmCallLogId;

    /**
     * 是否敏感内容，0 表示非敏感，1 表示敏感，便于后续内容审核和过滤
     */
    private Integer isSensitive;

    private LocalDateTime createTime;
}
