package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_browse_history")
public class UserBrowseHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long scenicSpotId;

    private Integer durationSeconds;

    private String source;

    private String deviceType;

    private LocalDateTime browseTime;

    private LocalDateTime createTime;
}
