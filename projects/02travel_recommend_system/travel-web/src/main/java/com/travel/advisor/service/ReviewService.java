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

    PageResult<ReviewReplyVO> pageReplies(Long reviewId, PageQuery query);
}
