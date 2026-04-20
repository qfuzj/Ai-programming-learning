package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐结果项实体
 */
@Data
@TableName("recommend_result_item")
public class RecommendResultItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的推荐记录ID
     */
    private Long recommendRecordId;

    /**
     * 推荐的景点ID
     */
    private Long scenicSpotId;

    /** 排名位置 */
    private Integer rankPosition;

    /** 推荐得分（0-1） */
    private Double score;

    /** 推荐理由 */
    private String reason;

    /** 是否被点击：0 否 1 是 */
    @TableField("is_clicked")
    private Integer isClicked;

    /** 是否被收藏：0 否 1 是 */
    @TableField("is_favorited")
    private Integer isFavorited;

    /** 点击时间 */
    private LocalDateTime clickTime;

    private LocalDateTime createTime;
}
