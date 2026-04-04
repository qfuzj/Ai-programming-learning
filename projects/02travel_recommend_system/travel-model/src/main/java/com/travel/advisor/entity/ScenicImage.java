package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景点图片实体类
 */
@Data
@TableName("scenic_image")
public class ScenicImage {

    // 主键，自增
    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联的景点 ID
    private Long scenicSpotId;

    // 关联的文件资源 ID
    private Long fileResourceId;

    // 图片访问地址（冗余字段，便于直接展示）
    private String imageUrl;

    // 图片类型：1 实景 2 地图 3 全景
    private Integer imageType;

    // 图片标题或描述
    private String title;

    // 排序值，数值越小优先展示
    private Integer sortOrder;

    // 是否为封面：0-否、1-是
    private Integer isCover;

    // 记录创建时间
    private LocalDateTime createTime;

    // 记录更新时间
    private LocalDateTime updateTime;

    // 逻辑删除标识：0-未删除、1-已删除
    @TableLogic
    private Integer isDeleted;
}