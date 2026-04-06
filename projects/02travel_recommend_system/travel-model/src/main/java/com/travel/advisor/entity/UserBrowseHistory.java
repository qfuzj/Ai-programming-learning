package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户浏览历史记录
 */
@Data
@TableName("user_browse_history")
public class UserBrowseHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long scenicSpotId;

    /**
     * 浏览时长，单位秒
     */
    private Integer durationSeconds;

    /**
     * 访问来源（search/recommend/share 等）
     */
    private String source;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 浏览时间
     */
    private LocalDateTime browseTime;

    private LocalDateTime createTime;
}
