package com.travel.advisor.controller.user;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.recommend.RecommendFeedbackDTO;
import com.travel.advisor.service.RecommendFeedbackService;
import com.travel.advisor.service.RecommendService;
import com.travel.advisor.vo.recommend.RecommendItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final RecommendFeedbackService recommendFeedbackService;

    @GetMapping("/home")
    public Result<PageResult<RecommendItemVO>> home(PageQuery pageQuery) {
        return Result.success(recommendService.homeRecommend(pageQuery));
    }

    @GetMapping("/scenic/{scenicId}")
    public Result<PageResult<RecommendItemVO>> similar(@PathVariable Long scenicId, PageQuery pageQuery) {
        return Result.success(recommendService.scenicSimilarRecommend(scenicId, pageQuery));
    }

    @PostMapping("/feedback/exposure")
    public Result<Void> exposure(@Valid @RequestBody RecommendFeedbackDTO dto) {
        recommendFeedbackService.exposure(dto);
        return Result.success();
    }

    @PostMapping("/feedback/click")
    public Result<Void> click(@Valid @RequestBody RecommendFeedbackDTO dto) {
        recommendFeedbackService.click(dto);
        return Result.success();
    }

    @PostMapping("/feedback/favorite")
    public Result<Void> favorite(@Valid @RequestBody RecommendFeedbackDTO dto) {
        recommendFeedbackService.favorite(dto);
        return Result.success();
    }
}
