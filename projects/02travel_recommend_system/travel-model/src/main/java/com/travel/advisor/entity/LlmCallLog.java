package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM 调用日志实体类
 */
@Data
@TableName("llm_call_log")
public class LlmCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 调用类型：chat / recommend / analyze / audit
     */
    private String callType;

    /**
     * 模型名称（gpt-4 / qwen-max等）
     */
    private String modelName;

    /**
     * 服务提供商（OpenAI / Baidu / Tencent等）
     */
    private String provider;

    /**
     * 请求 prompt
     */
    private String requestPrompt;

    /**
     * 完整请求（json）
     */
    private String requestMessages;

    /**
     * 响应内容
     */
    private String responseContent;

    /**
     * 输入 tokens 数量
     */
    private Integer inputTokens;

    /**
     * 输出 tokens 数量
     */
    private Integer outputTokens;

    /**
     * 总 tokens 数量
     */
    private Integer totalTokens;

    /**
     * 调用成本金额
     */
    private BigDecimal costAmount;

    /**
     * 响应时间，单位毫秒
     */
    private Integer responseTimeMs;

    /**
     * 调用状态：0-失败 1-成功 2-超时 3-限流
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 链路追踪 ID，用于关联分布式系统中的调用链路
     */
    private String traceId;

    /**
     * 请求IP地址
     */
    private String ipAddress;

    private LocalDateTime createTime;
}
