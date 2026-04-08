package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐记录表，用于记录每次推荐请求的详细信息，包括用户信息、推荐类型、请求参数、推荐结果等，用于分析推荐效果和优化算法
 */
@Data
@TableName("recommend_record")
public class RecommendRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 会话 ID，用于关联同一用户在同一会话中的多个推荐请求
     */
    private String sessionId;

    /**
     * 推荐类型：1 首页 2 相似 3 行程 4 搜索
     */
    private Integer recommendType;

    /**
     * 推荐场景，例如：景点推荐、酒店推荐、餐厅推荐等
     */
    private String scene;

    /**
     * 请求参数的 JSON 字符串，包含用户的偏好、历史行为等信息，用于分析推荐效果和优化算法
     */
    private String requestParams;

    /**
     * 用户画像快照的 JSON 字符串，记录推荐时的用户画像信息，包括兴趣、偏好、历史行为等，用于分析推荐效果和优化算法
     */
    private String userProfileSnapshot;

    /**
     * 推荐算法标识 使用的推荐算法版本或名称，例如：协同过滤、内容推荐、混合推荐等，用于分析不同算法的效果和优化算法选择
     */
    private String algorithm;

    /**
     * 是否使用 LLM（大语言模型）进行推荐，0 表示未使用，1 表示使用，用于分析 LLM 在推荐过程中的表现和优化 LLM 的使用
     */
    private Integer llmUsed;

    /**
     * 调用 LLM 的日志 ID，关联到 llm_call_log 表中的记录，用于分析 LLM 在推荐过程中的表现和优化 LLM 的使用
     */
    private Long llmCallLogId;

    /**
     * 推荐结果的 JSON 字符串，包含推荐的景点、酒店、餐厅等信息，用于分析推荐效果和优化算法
     */
    private Integer totalCandidates;

    /**
     * 实际返回给用户的推荐结果数量，用于分析推荐效果和优化算法
     */
    private Integer returnedCount;

    /**
     * 响应时间，单位毫秒，用于分析推荐系统的性能和优化响应速度
     */
    private Integer responseTimeMs;

    private LocalDateTime createTime;
}
