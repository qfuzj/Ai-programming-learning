package com.travel.advisor.vo.review;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDate visitDate;

    private String travelType;

    private Integer likeCount;

    private Integer replyCount;

    private Integer isAnonymous;

    private Integer status;

    private String auditRemark;

    private LocalDateTime createTime;
}
