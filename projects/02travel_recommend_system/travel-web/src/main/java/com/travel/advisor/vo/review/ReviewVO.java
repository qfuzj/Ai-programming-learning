package com.travel.advisor.vo.review;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户评论 VO
 */
@Data
public class ReviewVO {

    private Long id;

    private Long userId;

    private String username;

    private Long scenicId;

    private String scenicName;

    private Integer score;

    private String content;

    private List<Long> imageIds;

    /** 图片 URL 列表（前端展示用） */
    private List<String> images;

    /** 游玩日期 */
    private LocalDate visitDate;

    /**
     * 旅行类型：如家庭游、情侣游、朋友游等，帮助其他用户了解评论者的旅行背景和需求
     */
    private String travelType;

    /** 点赞数 */
    private Integer likeCount;

    /** 回复数 */
    private Integer replyCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /** 是否匿名 */
    private Integer isAnonymous;

    /**
     * 状态 （0-待审核，1-审核通过，2-审核不通过，3-隐藏）
     */
    private Integer status;

    /** 审核备注 */
    private String auditRemark;

    /**
     * 拒绝原因（与 auditRemark 同源字段，前端展示使用）
     */
    private String rejectReason;

    private LocalDateTime createdAt;
}
