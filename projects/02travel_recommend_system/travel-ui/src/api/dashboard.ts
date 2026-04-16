/**
 * 管理后台工作台接口。
 */
import http from "@/api/http";

/**
 * 对应-仪表盘概览数据VO
 */
export interface DashboardOverview {
  /** 用户总数 */
  totalUsers: number;
  /** 景点总数 */
  totalScenicSpots: number;
  /** 点评总数 */
  totalReviews: number;
  /** 旅行计划总数 */
  totalTravelPlans: number;
  /** 推荐请求总数（可选） */
  totalRecommendRequests?: number;
  /** LLM 调用总数 */
  totalLlmCalls: number;
  /** 浏览次数总数（可选） */
  totalBrowseCount?: number;
  /** 收藏总数（可选） */
  totalFavoriteCount?: number;
}

/**
 * 对应-景点热门排行榜VO类
 */
export interface ScenicHotRankingItem {
  /** 景点 ID */
  scenicId: number;
  /** 景点名称 */
  scenicName: string;
  /** 页面浏览量（可选） */
  pvCount?: number;
  /** 独立访客数（可选） */
  uvCount?: number;
  /** 收藏数（可选） */
  favoriteCount?: number;
  /** 评论数（可选） */
  reviewCount?: number;
  /** 推荐展示次数（可选） */
  recommendShowCount?: number;
  /** 推荐点击次数（可选） */
  recommendClickCount?: number;
  /** 平均评分（可选） */
  avgRating?: number;
}

/**
 * 时间序列数据点，用于图表展示。
 */
export interface TimeSeriesPoint {
  label: string;
  value: number;
}

/**
 * 对应-推荐分析VO
 */
export interface RecommendAnalysisSummary {
  /** 推荐请求总数（可选） */
  totalRecommendRequests?: number;
  /** 推荐点击总数（可选） */
  totalRecommendClicks?: number;
  /** 推荐收藏总数（可选） */
  totalRecommendFavorites?: number;
  /** 点击率（可选） */
  clickRate?: number;
  /** 收藏率（可选） */
  favoriteRate?: number;
  /** 日期列表（可选） */
  dates?: string[];
  /** 请求数量列表（可选） */
  requestCounts?: number[];
  /** 点击数量列表（可选） */
  clickCounts?: number[];
  /** 收藏数量列表（可选） */
  favoriteCounts?: number[];
}
/**
 * 对应-LLM调用分析结果VO类
 */
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

/**
 * 原始-仪表盘概览数据VO
 */
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

export function getDashboardOverview(): Promise<DashboardOverview> {
  return http.get<DashboardOverviewRaw>("/api/admin/dashboard/overview").then((res) => ({
    totalUsers: Number(res.totalUsers ?? 0),
    totalScenicSpots: Number(res.totalScenicSpots ?? 0),
    totalReviews: Number(res.totalReviews ?? 0),
    totalTravelPlans: Number(res.totalTravelPlans ?? 0),
    totalRecommendRequests: Number(res.totalRecommendRequests ?? 0),
    totalLlmCalls: Number(res.totalLlmCalls ?? 0),
    totalBrowseCount: Number(res.totalBrowseCount ?? 0),
    totalFavoriteCount: Number(res.totalFavoriteCount ?? 0),
  }));
}

export function getDashboardScenicHotRanking(): Promise<ScenicHotRankingItem[]> {
  return http.get<ScenicHotRankingItem[]>("/api/admin/dashboard/scenic-hot-ranking");
}

export function getDashboardRecommendAnalysis(): Promise<RecommendAnalysisSummary> {
  return http.get<RecommendAnalysisSummary>("/api/admin/dashboard/recommend-analysis");
}

export function getDashboardLlmAnalysis(): Promise<LlmAnalysisSummary> {
  return http.get<LlmAnalysisSummary>("/api/admin/dashboard/llm-analysis");
}
