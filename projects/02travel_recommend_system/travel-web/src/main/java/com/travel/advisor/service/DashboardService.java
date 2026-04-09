package com.travel.advisor.service;

import com.travel.advisor.vo.dashboard.DashboardOverviewVO;
import com.travel.advisor.vo.dashboard.LlmAnalysisVO;
import com.travel.advisor.vo.dashboard.RecommendAnalysisVO;
import com.travel.advisor.vo.dashboard.ScenicHotRankVO;

import java.util.List;

public interface DashboardService {

    DashboardOverviewVO overview();

    List<ScenicHotRankVO> scenicHotRanking();

    RecommendAnalysisVO recommendAnalysis();

    LlmAnalysisVO llmAnalysis();
}
