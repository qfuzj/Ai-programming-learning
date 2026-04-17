import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-推荐项VO类
 */
export interface AiRecommendItem {
  recommendRecordId?: number;
  resultItemId?: number;
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  score?: number;
  reason?: string;
  sourceType?: string;
  rankScore?: number;
}

/**
 * 对应-推荐反馈DTO
 */
export interface RecommendFeedbackPayload {
  recommendRecordId: number;
  resultItemId: number;
  scenicId: number;
}

export function fetchAiRecommendations(
  query: Partial<PageQuery> = { pageNum: 1, pageSize: 10 }
): Promise<PageResult<AiRecommendItem>> {
  return http.get<PageResult<AiRecommendItem>>("/api/user/recommend/home", { params: query });
}

export function fetchSimilarRecommendations(
  scenicId: number,
  query: Partial<PageQuery> = { pageNum: 1, pageSize: 10 }
): Promise<PageResult<AiRecommendItem>> {
  return http.get<PageResult<AiRecommendItem>>(`/api/user/recommend/scenic/${scenicId}`, {
    params: query,
  });
}

export function sendRecommendExposure(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post<void>("/api/user/recommend/feedback/exposure", payload);
}

export function sendRecommendClick(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post<void>("/api/user/recommend/feedback/click", payload);
}

export function sendRecommendFavorite(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post<void>("/api/user/recommend/feedback/favorite", payload);
}
