package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景点标签关联实体
 */
@Data
@TableName("scenic_spot_tag")
public class ScenicSpotTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicSpotId;

    private Long tagId;

    private LocalDateTime createTime;
}