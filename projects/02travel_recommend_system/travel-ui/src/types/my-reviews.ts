import type { ReviewItem } from "@/api/audit";

/**
 * 我的评论相关类型定义。
 */
export interface MyReviewForm {
  scenicId: number;
  score: number;
  content: string;
  visitDate: string;
  travelType: string;
  isAnonymousBool: boolean;
}

export interface MyReviewsPageState {
  reviews: ReviewItem[];
  total: number;
  currentPage: number;
  pageSize: number;
}
