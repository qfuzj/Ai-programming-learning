package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户评价实体类
 */
@Data
@TableName("user_review")
public class UserReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long scenicSpotId;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片
     */
    private String images;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 出行类型
     */
    private String travelType;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 是否匿名评价（0-否，1-是）
     */
    private Integer isAnonymous;

    /**
     * 状态 （0-待审核，1-审核通过，2-审核不通过，3-隐藏）
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 发布IP地址
     */
    private String ipAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
