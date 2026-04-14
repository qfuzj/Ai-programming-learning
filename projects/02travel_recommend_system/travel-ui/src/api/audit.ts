/**
 * 点评模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

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
  isAnonymous?: number;
  status?: number;
  auditRemark?: string;
  createTime?: string;
}

export interface ReviewQuery extends PageQuery {
  contentType?: "review" | "scenic" | "plan";
  auditStatus?: number;
  submitUserId?: number;
  contentId?: number;
}

interface AdminAuditRaw {
  id: number;
  contentType?: string;
  contentId?: number;
  contentSnapshot?: string;
  submitUserId?: number;
  auditStatus?: number;
  createTime?: string;
}

interface ReviewSnapshot {
  scenicId?: number;
  scenicName?: string;
  userId?: number;
  username?: string;
  content?: string;
  score?: number;
  rating?: number;
  images?: string[];
}

function parseReviewSnapshot(snapshot?: string): ReviewSnapshot {
  if (!snapshot) {
    return {};
  }
  try {
    const parsed: unknown = JSON.parse(snapshot);
    if (parsed && typeof parsed === "object") {
      return parsed as ReviewSnapshot;
    }
    return {};
  } catch {
    return {};
  }
}

function mapAuditToReview(raw: AdminAuditRaw): ReviewItem {
  const snapshot = parseReviewSnapshot(raw.contentSnapshot);
  return {
    id: raw.id,
    userId: snapshot.userId ?? raw.submitUserId,
    username: snapshot.username,
    scenicId: snapshot.scenicId,
    scenicName: snapshot.scenicName,
    content: snapshot.content,
    rating: snapshot.rating ?? snapshot.score,
    score: snapshot.score ?? snapshot.rating,
    status: raw.auditStatus,
    createTime: raw.createTime,
    images: snapshot.images,
  };
}

function mapAuditPage(page: PageResult<AdminAuditRaw>): PageResult<ReviewItem> {
  return {
    ...page,
    records: page.records.map(mapAuditToReview),
  };
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

export async function getAdminAuditPage(query: ReviewQuery): Promise<PageResult<ReviewItem>> {
  const page = await http.get<any, PageResult<AdminAuditRaw>>("/api/admin/audits", {
    params: {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      contentType: query.contentType ?? "review",
      auditStatus: query.auditStatus,
      submitUserId: query.submitUserId,
      contentId: query.contentId,
    },
  });
  return mapAuditPage(page);
}

export async function getAdminAuditDetail(id: number): Promise<ReviewItem> {
  const detail = await http.get<any, AdminAuditRaw>(`/api/admin/audits/${id}`);
  return mapAuditToReview(detail);
}

export function approveAdminAudit(id: number): Promise<void> {
  return http.post(`/api/admin/audits/${id}/approve`, {});
}

export function rejectAdminAudit(id: number, reason: string): Promise<void> {
  return http.post(`/api/admin/audits/${id}/reject`, { reason });
}

export function hideAdminAudit(id: number): Promise<void> {
  return http.post(`/api/admin/audits/${id}/hide`, {});
}

// Backward-compatible aliases for existing imports.
export const getAdminReviewPage = getAdminAuditPage;
export const getAdminReviewDetail = getAdminAuditDetail;
export const approveAdminReview = approveAdminAudit;
export const rejectAdminReview = rejectAdminAudit;
export const deleteAdminReview = hideAdminAudit;