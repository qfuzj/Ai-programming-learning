package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_review")
public class UserReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long scenicSpotId;

    private Integer rating;

    private String content;

    private String images;

    private LocalDate visitDate;

    private String travelType;

    private Integer likeCount;

    private Integer replyCount;

    private Integer isAnonymous;

    private Integer status;

    private String auditRemark;

    private String ipAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
