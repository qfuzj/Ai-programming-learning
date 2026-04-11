/**
 * 管理后台工作台接口。
 */
import http from "@/api/http";

export interface DashboardOverview {
  totalUsers: number;
  totalScenicSpots: number;
  totalReviews: number;
  totalPlans: number;
  totalRecommendRequests?: number;
  totalLlmCalls: number;
  totalBrowseCount?: number;
  totalFavoriteCount?: number;
}

export interface ScenicHotRankingItem {
  scenicId: number;
  scenicName: string;
  heatScore: number;
  pvCount?: number;
  uvCount?: number;
  favoriteCount?: number;
  reviewCount?: number;
  recommendShowCount?: number;
  recommendClickCount?: number;
  avgRating?: number;
}

export interface TimeSeriesPoint {
  label: string;
  value: number;
}

export interface RecommendAnalysisSummary {
  totalRecommendRequests?: number;
  totalRecommendClicks?: number;
  totalRecommendFavorites?: number;
  clickRate?: number;
  favoriteRate?: number;
  dates?: string[];
  requestCounts?: number[];
  clickCounts?: number[];
  favoriteCounts?: number[];
}

export interface LlmAnalysisSummary {
  totalCallCount?: number;
  successCallCount?: number;
  failCallCount?: number;
  totalTokens?: number;
  totalCostAmount?: number;
  dates?: string[];
  callCounts?: number[];
  costAmounts?: number[];
}

interface DashboardOverviewRaw {
  totalUsers?: number;
  totalScenicSpots?: number;
  totalReviews?: number;
  totalTravelPlans?: number;
  totalRecommendRequests?: number;
  totalLlmCalls?: number;
  totalBrowseCount?: number;
  totalFavoriteCount?: number;
}

interface ScenicHotRankingRaw {
  scenicId: number;
  scenicName: string;
  pvCount?: number;
  uvCount?: number;
  favoriteCount?: number;
  reviewCount?: number;
  recommendShowCount?: number;
  recommendClickCount?: number;
  avgRating?: number;
}

export function getDashboardOverview(): Promise<DashboardOverview> {
  return http.get<any, DashboardOverviewRaw>("/api/admin/dashboard/overview").then((res) => ({
    totalUsers: Number(res.totalUsers ?? 0),
    totalScenicSpots: Number(res.totalScenicSpots ?? 0),
    totalReviews: Number(res.totalReviews ?? 0),
    totalPlans: Number(res.totalTravelPlans ?? 0),
    totalRecommendRequests: Number(res.totalRecommendRequests ?? 0),
    totalLlmCalls: Number(res.totalLlmCalls ?? 0),
    totalBrowseCount: Number(res.totalBrowseCount ?? 0),
    totalFavoriteCount: Number(res.totalFavoriteCount ?? 0),
  }));
}

export function getDashboardScenicHotRanking(): Promise<ScenicHotRankingItem[]> {
  return http
    .get<any, ScenicHotRankingRaw[]>("/api/admin/dashboard/scenic-hot-ranking")
    .then((res) =>
      res.map((item) => ({
        scenicId: item.scenicId,
        scenicName: item.scenicName,
        heatScore: Number(item.pvCount ?? 0),
        pvCount: item.pvCount,
        uvCount: item.uvCount,
        favoriteCount: item.favoriteCount,
        reviewCount: item.reviewCount,
        recommendShowCount: item.recommendShowCount,
        recommendClickCount: item.recommendClickCount,
        avgRating: item.avgRating,
      }))
    );
}

export function getDashboardRecommendAnalysis(): Promise<RecommendAnalysisSummary> {
  return http.get("/api/admin/dashboard/recommend-analysis");
}

export function getDashboardLlmAnalysis(): Promise<LlmAnalysisSummary> {
  return http.get("/api/admin/dashboard/llm-analysis");
}
