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

    /** 会话 ID */
    private Long conversationId;

    /** 角色：user / assistant / system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 内容类型：1 文本 2 图片 3 卡片 */
    private Integer contentType;

    /**
     * 附加数据，JSON格式，如推荐景点列表
     */
    private String extraData;

    /** 消耗 tokens */
    private Integer tokensUsed;

    /** 关联 LLM 调用日志 ID */
    private Long llmCallLogId;

    /** 是否敏感：0 否 1 是 */
    private Integer isSensitive;

    private LocalDateTime createTime;
}
