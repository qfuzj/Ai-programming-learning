package com.travel.advisor.vo.history;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户浏览历史记录VO
 */
@Data
public class BrowseHistoryVO {

    private Long id;

    private Long scenicId;

    private String scenicName;

    private String coverImage;

    private Integer stayDuration;

    private String source;

    private String deviceType;

    private LocalDateTime browseTime;
}
