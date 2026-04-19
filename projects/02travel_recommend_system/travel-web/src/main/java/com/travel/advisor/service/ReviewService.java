package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.review.ReviewCreateDTO;
import com.travel.advisor.dto.review.ReviewReplyDTO;
import com.travel.advisor.vo.review.ReviewReplyVO;
import com.travel.advisor.vo.review.ReviewVO;

public interface ReviewService {

    Long create(ReviewCreateDTO dto);

    PageResult<ReviewVO> pageMyReviews(PageQuery pageQuery);

    void deleteMyReview(Long id);

    PageResult<ReviewVO> pageScenicReviews(Long scenicId, PageQuery pageQuery);

    void likeReview(Long reviewId);

    Long replyReview(Long reviewId, ReviewReplyDTO dto);

    /**
     * 分页查询评论回复。
     *
     * @param reviewId 评论 ID
     * @param query    分页参数
     * @param sortBy   排序模式：{@code time_asc}（默认，最早在前）、{@code time_desc}、
     *                 {@code hot}（保留关键字，当前无 like_count 字段，等价 time_desc）。
     *                 未来若 {@code review_reply} 表引入热度字段，该分支将切换为真正的热度排序。
     */
    PageResult<ReviewReplyVO> pageReplies(Long reviewId, PageQuery query, String sortBy);
}
