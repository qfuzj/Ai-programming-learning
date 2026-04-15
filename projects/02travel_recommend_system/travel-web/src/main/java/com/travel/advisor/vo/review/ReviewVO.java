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

    /**
     * 用户访问景点的日期，便于其他用户了解评论的时效性和相关性
     */
    private LocalDate visitDate;

    /**
     * 旅行类型：如家庭游、情侣游、朋友游等，帮助其他用户了解评论者的旅行背景和需求
     */
    private String travelType;

    /**
     * 点赞数，记录有多少用户觉得这个评论有帮助，便于其他用户判断评论的质量和可信度
     */
    private Integer likeCount;

    /**
     * 回复数，记录有多少用户对这个评论进行了回复，便于其他用户了解评论的互动情况和讨论热度
     */
    private Integer replyCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 是否匿名评论，匿名评论不显示用户名和头像，保护用户隐私，同时也可能影响评论的可信度和互动情况
     */
    private Integer isAnonymous;

    /**
     * 状态 （0-待审核，1-审核通过，2-审核不通过，3-隐藏）
     */
    private Integer status;

    /**
     * 审核备注，记录审核人员对评论的审核意见和理由，便于后续的审核复核和用户申诉处理
     */
    private String auditRemark;

    private LocalDateTime createdAt;
}
