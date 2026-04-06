package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.review.ReviewCreateDTO;
import com.travel.advisor.service.ReviewService;
import com.travel.advisor.vo.review.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public Result<Long> create(@Valid @RequestBody ReviewCreateDTO dto) {
        return Result.success(reviewService.create(dto));
    }

    @GetMapping("/reviews/me")
    public Result<PageResult<ReviewVO>> myReviews(PageQuery pageQuery) {
        return Result.success(reviewService.pageMyReviews(pageQuery));
    }

    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteMyReview(@PathVariable Long id) {
        reviewService.deleteMyReview(id);
        return Result.success();
    }

    @GetMapping("/scenic-spots/{scenicId}/reviews")
    public Result<PageResult<ReviewVO>> scenicReviews(@PathVariable Long scenicId, PageQuery pageQuery) {
        return Result.success(reviewService.pageScenicReviews(scenicId, pageQuery));
    }
}
