package com.travel.advisor.vo.review;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论回复VO（Review Reply View Object）
 *
 * 表示用户对评论的回复信息，用于前端展示评论下的回复列表。
 *
 * 设计说明：
 * - 属于评论（Review）的子资源
 * - 用于构建评论 → 回复 的层级结构
 * - 不包含点赞、审核等扩展信息（如需可扩展）
 */
@Data
public class ReviewReplyVO {

    /**
     * 回复ID（唯一标识）
     */
    private Long id;

    /**
     * 所属评论ID
     */
    private Long reviewId;

    /**
     * 回复用户ID
     */
    private Long userId;

    /**
     * 回复用户昵称（冗余字段，避免前端二次查询用户信息）
     */
    private String username;

    /**
     * 回复内容（纯文本）
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}