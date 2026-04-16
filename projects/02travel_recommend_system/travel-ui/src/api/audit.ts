/**
 * 点评审核模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-用户评论 VO
 */
export interface ReviewItem {
  id: number;
  userId?: number;
  username?: string;
  scenicId?: number;
  scenicName?: string;
  score?: number;
  rating?: number;
  content?: string;
  imageIds?: number[];
  images?: string[];
  visitDate?: string;
  travelType?: string;
  likeCount?: number;
  replyCount?: number;
  isLiked?: boolean;
  isAnonymous?: number;
  status?: number;
  auditRemark?: string;
  createdAt?: string;
}
/**
 * 对应-审核记录VO
 */
export interface AuditItem {
  id: number;
  contentType?: string;
  contentId?: number;
  snapshot?: any;
  submitUserId?: number;
  auditStatus?: number;
  autoAuditResult?: any;
  autoAuditScore?: number;
  llmCallLogId?: number;
  auditorId?: number;
  auditRemark?: string;
  auditTime?: string;
  violationType?: any;
  createTime?: string;
  updateTime?: string;
}
/**
 * 审核查询DTO
 */
export interface AuditQuery extends PageQuery {
  contentType?: string;
  auditStatus?: number;
  submitUserId?: number;
  contentId?: number;
}

/**
 * 获取我的评论列表
 */
export function getMyReviews(query: PageQuery): Promise<PageResult<ReviewItem>> {
  return http.get("/api/user/reviews/me", { params: query });
}

/**
 * 删除我的评论
 */
export function deleteMyReview(id: number): Promise<void> {
  return http.delete(`/api/user/reviews/${id}`);
}

/**
 * 获取景点评论列表
 */
export function getScenicReviews(
  scenicId: number,
  query: PageQuery
): Promise<PageResult<ReviewItem>> {
  return http.get(`/api/user/scenic-spots/${scenicId}/reviews`, { params: query });
}

/**
 * 提交评论
 */
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

/**
 * 获取管理员审核分页列表
 */
export async function getAdminAuditPage(query: AuditQuery): Promise<PageResult<AuditItem>> {
  return http.get<PageResult<AuditItem>>("/api/admin/audits", {
    params: {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      contentType: query.contentType ?? "review",
      auditStatus: query.auditStatus,
      submitUserId: query.submitUserId,
      contentId: query.contentId,
    },
  });
}

/**
 * 获取管理员审核详情
 */
export async function getAdminAuditDetail(id: number): Promise<AuditItem> {
  return http.get<AuditItem>(`/api/admin/audits/${id}`);
}

/**
 * 批准审核
 */
export function approveAdminAudit(id: number): Promise<void> {
  return http.post(`/api/admin/audits/${id}/approve`, {});
}

/**
 * 拒绝审核
 */
export function rejectAdminAudit(id: number, reason: string): Promise<void> {
  return http.post(`/api/admin/audits/${id}/reject`, { reason });
}

/**
 * 隐藏审核
 */
export function hideAdminAudit(id: number): Promise<void> {
  return http.post(`/api/admin/audits/${id}/hide`, {});
}

/**
 * 对应-评论回复VO
 */
export interface ReviewReplyItem {
  id: number;
  reviewId: number;
  userId: number;
  username: string;
  content: string;
  createdAt: string;
}

/**
 * 点赞评论
 */
export function likeReview(reviewId: number): Promise<void> {
  return http.post(`/api/user/reviews/${reviewId}/like`);
}

/**
 * 回复评论
 */
export function replyReview(reviewId: number, content: string): Promise<number> {
  return http.post(`/api/user/reviews/${reviewId}/reply`, { content });
}

/**
 * 分页查询评论回复列表
 */
export function getReviewReplies(
  reviewId: number,
  query: PageQuery
): Promise<PageResult<ReviewReplyItem>> {
  return http.get(`/api/user/reviews/${reviewId}/replies`, { params: query });
}
