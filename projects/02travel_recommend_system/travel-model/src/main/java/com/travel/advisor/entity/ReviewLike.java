package com.travel.advisor.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 评价点赞关联表（中间表）
 * <p>
 * 记录用户对评价的点赞关系，解决用户与评价之间的多对多关联。
 * 每个点赞记录表示某个用户对某条评价进行了一次点赞操作。
 * </p>
 *
 */
@Data
@TableName("review_like")
public class ReviewLike {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID（业务外键）
     * 关联 {@code review} 表的主键
     */
    private Long reviewId;

    /**
     * 用户ID（业务外键）
     * 关联 {@code user} 表的主键
     */
    private Long userId;

    /**
     * 点赞创建时间
     * 即用户点击“点赞”的时刻
     */
    private LocalDateTime createTime;

    /**
     * 点赞更新时间
     * 通常与创建时间一致，若业务需要支持“取消点赞后再次点赞”可记录最新点赞时间
     */
    private LocalDateTime updateTime;

}