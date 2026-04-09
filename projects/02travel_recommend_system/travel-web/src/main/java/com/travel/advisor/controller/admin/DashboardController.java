package com.travel.advisor.controller.admin;

import com.travel.advisor.common.result.Result;
import com.travel.advisor.service.DashboardService;
import com.travel.advisor.vo.dashboard.DashboardOverviewVO;
import com.travel.advisor.vo.dashboard.LlmAnalysisVO;
import com.travel.advisor.vo.dashboard.RecommendAnalysisVO;
import com.travel.advisor.vo.dashboard.ScenicHotRankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return Result.success(dashboardService.overview());
    }

    @GetMapping("/scenic-hot-ranking")
    public Result<List<ScenicHotRankVO>> scenicHotRanking() {
        return Result.success(dashboardService.scenicHotRanking());
    }

    @GetMapping("/recommend-analysis")
    public Result<RecommendAnalysisVO> recommendAnalysis() {
        return Result.success(dashboardService.recommendAnalysis());
    }

    @GetMapping("/llm-analysis")
    public Result<LlmAnalysisVO> llmAnalysis() {
        return Result.success(dashboardService.llmAnalysis());
    }
}
