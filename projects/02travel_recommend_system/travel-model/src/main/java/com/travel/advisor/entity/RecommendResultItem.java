package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
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

    /**
     * 推荐结果中的排名位置，从1开始，表示第几位推荐
     */
    private Integer rankPosition;

    /**
     * 推荐算法计算出的得分，数值越大表示推荐结果越相关，范围通常在0到1之间
     */
    private BigDecimal score;

    /**
     * 推荐理由，简要说明为什么这个景点被推荐给用户，可以是算法生成的文本或者预定义的模板内容
     */
    private String reason;

    /**
     * 用户交互状态，表示用户是否点击过该推荐结果，0表示未点击，1表示已点击
     */
    @TableField("is_clicked")
    private Integer isClicked;

    /**
     * 用户收藏状态，表示用户是否收藏过该推荐结果，0表示未收藏，1表示已收藏
     */
    @TableField("is_favorited")
    private Integer isFavorited;

    /**
     * 用户点击推荐结果的时间，只有当isClicked为1时才有值，记录用户点击推荐结果的具体时间点，用于分析用户行为和优化推荐算法
     */
    private LocalDateTime clickTime;

    private LocalDateTime createTime;
}
