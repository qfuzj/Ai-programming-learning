package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 推荐记录 */
@Data
@TableName("recommend_record")
public class RecommendRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 会话 ID */
    private String sessionId;

    /**
     * 推荐类型：1 首页 2 相似 3 行程 4 搜索
     */
    private Integer recommendType;

    /**
     * 推荐场景，例如：景点推荐、酒店推荐、餐厅推荐等
     */
    private String scene;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 用户画像快照（JSON） */
    private String userProfileSnapshot;

    /** 推荐算法标识 */
    private String algorithm;

    /** 是否使用 LLM：0 否 1 是 */
    private Integer llmUsed;

    /** 关联 LLM 调用记录 ID */
    private Long llmCallLogId;

    /** 候选集总数 */
    private Integer totalCandidates;

    /** 返回结果数 */
    private Integer returnedCount;

    /** 响应耗时（ms） */
    private Integer responseTimeMs;

    private LocalDateTime createTime;
}
