package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复实体类（一对多子表）
 * <p>
 * 存储用户对某条评价（或回复本身，若支持嵌套回复）的回复内容。
 * 与 {@code review} 表形成一对多关系：一条评价可拥有多条回复。
 * 支持逻辑删除，保留历史回复记录。
 * </p>
 *
 */
@Data
@TableName("review_reply")
public class ReviewReply {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的评价ID（业务外键）
     * 指向 {@code review} 表的主键，表示该回复属于哪条评价
     */
    private Long reviewId;

    /**
     * 回复者用户ID（业务外键）
     * 指向 {@code user} 表的主键，表示谁发表的回复
     */
    private Long userId;

    /**
     * 回复内容（纯文本或富文本，建议限制长度）
     */
    private String content;

    /**
     * 回复创建时间（首次发表时间）
     */
    private LocalDateTime createTime;

    /**
     * 回复更新时间（编辑回复时更新）
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * <p>
     * 0 - 未删除；1 - 已删除（MyBatis-Plus 逻辑删除支持）
     * 删除回复时并不物理移除，仅标记删除，保证数据可追溯
     * </p>
     */
    @TableLogic
    private Integer isDeleted;

}