package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scenic_image")
public class ScenicImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicSpotId;

    private Long fileResourceId;

    private String imageUrl;

    private Integer imageType;

    private String title;

    private Integer sortOrder;

    private Integer isCover;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}