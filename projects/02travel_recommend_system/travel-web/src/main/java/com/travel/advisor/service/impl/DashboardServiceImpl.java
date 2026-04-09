package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.travel.advisor.entity.*;
import com.travel.advisor.mapper.*;
import com.travel.advisor.service.DashboardService;
import com.travel.advisor.vo.dashboard.DashboardOverviewVO;
import com.travel.advisor.vo.dashboard.LlmAnalysisVO;
import com.travel.advisor.vo.dashboard.RecommendAnalysisVO;
import com.travel.advisor.vo.dashboard.ScenicHotRankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final UserReviewMapper userReviewMapper;
    private final TravelPlanMapper travelPlanMapper;
    private final RecommendRecordMapper recommendRecordMapper;
    private final RecommendResultItemMapper recommendResultItemMapper;
    private final LlmCallLogMapper llmCallLogMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final StatScenicDailyMapper statScenicDailyMapper;

    /**
     * 概览统计，返回平台的整体数据概况，包括用户总数、景点总数、评论总数、旅行计划总数、推荐请求总数、LLM调用总数、浏览总数和收藏总数等关键指标。
     */
    @Override
    @Cacheable(value = "dashboard:overview", key = "'overview'")
    public DashboardOverviewVO overview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTotalScenicSpots(scenicSpotMapper.selectCount(null));
        vo.setTotalReviews(userReviewMapper.selectCount(null));
        vo.setTotalTravelPlans(travelPlanMapper.selectCount(null));
        vo.setTotalRecommendRequests(recommendRecordMapper.selectCount(null));
        vo.setTotalLlmCalls(llmCallLogMapper.selectCount(null));
        vo.setTotalBrowseCount(userBrowseHistoryMapper.selectCount(null));
        vo.setTotalFavoriteCount(userFavoriteMapper.selectCount(null));
        return vo;
    }

    /**
     * 景点热度排行榜，统计最近30天内各景点的浏览量、独立访客数、收藏数、评论数、推荐展示和点击数，以及平均评分等指标，并返回热度排名前10的景点列表。
     */
    @Override
    public List<ScenicHotRankVO> scenicHotRanking() {
        List<Map<String, Object>> rows = statScenicDailyMapper.selectMaps(new QueryWrapper<StatScenicDaily>()
                .select("scenic_spot_id as scenicSpotId",
                        "SUM(pv_count) as pvCount",
                        "SUM(uv_count) as uvCount",
                        "SUM(favorite_count) as favoriteCount",
                        "SUM(review_count) as reviewCount",
                        "SUM(recommend_show_count) as recommendShowCount",
                        "SUM(recommend_click_count) as recommendClickCount",
                        "SUM(avg_rating * review_count) / NULLIF(SUM(review_count), 0) as avgRating")
                .ge("stat_date", LocalDate.now().minusDays(30))
                .groupBy("scenic_spot_id")
                .orderByDesc("pvCount")
                .last("limit 10"));
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> scenicIds = rows.stream()
                .map(row -> toLong(row.get("scenicSpotId")))
                .filter(Objects::nonNull)
                .toList();
        Map<Long, ScenicSpot> scenicMap = new HashMap<>();
        scenicSpotMapper.selectBatchIds(scenicIds).forEach(item -> scenicMap.put(item.getId(), item));

        List<ScenicHotRankVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long scenicId = toLong(row.get("scenicSpotId"));
            ScenicSpot scenicSpot = scenicMap.get(scenicId);
            ScenicHotRankVO vo = new ScenicHotRankVO();
            vo.setScenicId(scenicId);
            vo.setScenicName(scenicSpot == null ? null : scenicSpot.getName());
            vo.setPvCount(toInt(row.get("pvCount")));
            vo.setUvCount(toInt(row.get("uvCount")));
            vo.setFavoriteCount(toInt(row.get("favoriteCount")));
            vo.setReviewCount(toInt(row.get("reviewCount")));
            vo.setRecommendShowCount(toInt(row.get("recommendShowCount")));
            vo.setRecommendClickCount(toInt(row.get("recommendClickCount")));
            Object avgRating = row.get("avgRating");
            vo.setAvgRating(avgRating == null ? null : toBigDecimal(avgRating));
            result.add(vo);
        }
        return result;
    }

    /**
     * 推荐分析
     */
    @Override
    public RecommendAnalysisVO recommendAnalysis() {
        // 推荐请求总数、点击总数、收藏总数
        long totalRequests = recommendRecordMapper.selectCount(null);
        long totalClicks = recommendResultItemMapper.selectCount(new LambdaQueryWrapper<RecommendResultItem>()
                .eq(RecommendResultItem::getIsClicked, 1));      // 1表示已点击
        long totalFavorites = recommendResultItemMapper.selectCount(new LambdaQueryWrapper<RecommendResultItem>()
                .eq(RecommendResultItem::getIsFavorited, 1));    // 1表示已收藏

        RecommendAnalysisVO vo = new RecommendAnalysisVO();
        vo.setTotalRecommendRequests(totalRequests);
        vo.setTotalRecommendClicks(totalClicks);
        vo.setTotalRecommendFavorites(totalFavorites);
        vo.setClickRate(totalRequests == 0 ? 0D : totalClicks * 1D / totalRequests);
        vo.setFavoriteRate(totalRequests == 0 ? 0D : totalFavorites * 1D / totalRequests);

        // 最近7天的推荐请求数、点击数、收藏数趋势
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        List<RecommendRecord> recentRecords = recommendRecordMapper.selectList(new LambdaQueryWrapper<RecommendRecord>()
                .ge(RecommendRecord::getCreateTime, start)
                .orderByAsc(RecommendRecord::getCreateTime));
        List<RecommendResultItem> recentItems = recommendResultItemMapper.selectList(new LambdaQueryWrapper<RecommendResultItem>()
                .ge(RecommendResultItem::getCreateTime, start)
                .orderByAsc(RecommendResultItem::getCreateTime));
        fillTrend(vo, recentRecords, recentItems);
        return vo;
    }

    /**
     * LLM 调用分析
     */
    @Override
    public LlmAnalysisVO llmAnalysis() {

        // 最近7天的数据用于趋势分析
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        List<LlmCallLog> recentLogs = llmCallLogMapper.selectList(new LambdaQueryWrapper<LlmCallLog>()
                .ge(LlmCallLog::getCreateTime, start)
                .orderByAsc(LlmCallLog::getCreateTime));

        LlmAnalysisVO vo = new LlmAnalysisVO();

        // 全量汇总统计（单 SQL）
        Map<String, Object> summary = llmCallLogMapper.selectCallLogSummary();
        vo.setTotalCallCount(toLong(summary.get("totalCallCount")));
        vo.setSuccessCallCount(toLong(summary.get("successCallCount")));
        vo.setFailCallCount(toLong(summary.get("failCallCount")));
        vo.setTotalTokens(toLong(summary.get("totalTokens")));
        vo.setTotalCostAmount(toBigDecimal(summary.get("totalCostAmount")));

        fillLlmTrend(vo, recentLogs);
        return vo;
    }

    /**
     * 填充推荐趋势数据
     */
    private void fillTrend(RecommendAnalysisVO vo, List<RecommendRecord> records, List<RecommendResultItem> items) {
        // 按日期统计推荐请求数
        Map<LocalDate, Long> requestMap = records.stream()
                .filter(item -> item.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 按日期统计点击数
        Map<LocalDate, Long> clickMap = items.stream()
                .filter(item -> item.getCreateTime() != null)
                .filter(item -> Integer.valueOf(1).equals(item.getIsClicked()))
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 按日期统计收藏数
        Map<LocalDate, Long> favoriteMap = items.stream()
                .filter(item -> item.getCreateTime() != null)
                .filter(item -> Integer.valueOf(1).equals(item.getIsFavorited()))
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));


        // 构建最近7天的日期列表和对应的统计数据，填充到VO中
        List<String> dates = new ArrayList<>();
        List<Long> requestCounts = new ArrayList<>();
        List<Long> clickCounts = new ArrayList<>();
        List<Long> favoriteCounts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            requestCounts.add(requestMap.getOrDefault(date, 0L));
            clickCounts.add(clickMap.getOrDefault(date, 0L));
            favoriteCounts.add(favoriteMap.getOrDefault(date, 0L));
        }

        vo.setDates(dates);
        vo.setRequestCounts(requestCounts);
        vo.setClickCounts(clickCounts);  // 每日点击数
        vo.setFavoriteCounts(favoriteCounts); // 每日收藏数
    }

    /**
     * 填充LLM调用趋势数据
     */
    private void fillLlmTrend(LlmAnalysisVO vo, List<LlmCallLog> logs) {
        // 按日期统计调用次数
        Map<LocalDate, Long> countMap = logs.stream()
                .filter(item -> item.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        // 按日期统计消耗金额
        Map<LocalDate, BigDecimal> costMap = logs.stream().filter(item -> item.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> item.getCostAmount() == null ? BigDecimal.ZERO : item.getCostAmount(),
                                BigDecimal::add)
                ));

        // 构建7天的日期列表和对应的统计数据，填充到VO中

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> costs = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            counts.add(countMap.getOrDefault(date, 0L));
            costs.add(costMap.getOrDefault(date, BigDecimal.ZERO));
        }
        vo.setDates(dates);
        vo.setCallCounts(counts);
        vo.setCostAmounts(costs);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(String.valueOf(value));
    }
}
