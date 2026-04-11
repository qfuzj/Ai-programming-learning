/**
 * 点评模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface ReviewItem {
  id: number;
  userId?: number;
  username?: string;
  scenicId: number;
  scenicName: string;
  score: number;
  content: string;
  imageIds?: number[];
  visitDate?: string;
  travelType?: string;
  likeCount?: number;
  replyCount?: number;
  isAnonymous?: number;
  status?: number;
  auditRemark?: string;
  createTime?: string;
}

export function getMyReviews(query: PageQuery): Promise<PageResult<ReviewItem>> {
  return http.get("/api/user/reviews/me", { params: query });
}

export function deleteMyReview(id: number): Promise<void> {
  return http.delete(`/api/user/reviews/${id}`);
}

export function getScenicReviews(
  scenicId: number,
  query: PageQuery
): Promise<PageResult<ReviewItem>> {
  return http.get(`/api/user/scenic-spots/${scenicId}/reviews`, { params: query });
}

export function submitReview(payload: {
  scenicId: number;
  score: number;
  content: string;
  imageIds?: number[];
  visitDate?: string;
  travelType?: string;
  isAnonymous?: number;
}): Promise<number> {
  return http.post("/api/user/reviews", payload);
}
