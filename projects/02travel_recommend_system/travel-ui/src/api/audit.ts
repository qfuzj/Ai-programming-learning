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
  rejectReason?: string;
  createdAt?: string;
}

/**
 * 景点评论列表列表-前端缓存
 */
interface ScenicReviewCacheEntry {
  data: PageResult<ReviewItem>;
  expireAt: number;
}

// 5分钟缓存过期，最多缓存100个不同查询的结果
const CACHE_TTL_MS = 5 * 60 * 1000;
const CACHE_MAX_ENTRIES = 100;
const scenicReviewsCache = new Map<string, ScenicReviewCacheEntry>();

function normalizeReviewItem(item: ReviewItem): ReviewItem {
  const resolvedScore = item.score ?? item.rating;
  return {
    ...item,
    score: resolvedScore,
    rating: resolvedScore,
  };
}

function normalizeReviewPage(page: PageResult<ReviewItem>): PageResult<ReviewItem> {
  return {
    ...page,
    records: (page.records || []).map(normalizeReviewItem),
  };
}

function cloneReviewPage(page: PageResult<ReviewItem>): PageResult<ReviewItem> {
  return {
    ...page,
    records: [...(page.records || [])],
  };
}
/**
 * 将查询参数排序后序列化，确保不同顺序但内容相同的参数生成同一个 key。
 */
function buildStableQueryString(query: PageQuery): string {
  const sortedEntries = Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null)
    .sort(([a], [b]) => a.localeCompare(b));
  return JSON.stringify(Object.fromEntries(sortedEntries));
}

/**
 * 构建景点评论缓存的 key，格式为 `${scenicId}:${sortedQueryString}`，其中 sortedQueryString 是对查询参数进行排序后序列化得到的字符串。通过这种方式，可以确保对于相同的 scenicId 和查询参数，无论参数的顺序如何，都能生成相同的缓存 key，从而提高缓存命中率。
 */
function buildScenicReviewsCacheKey(scenicId: number, query: PageQuery): string {
  return `${scenicId}:${buildStableQueryString(query)}`;
}

function getCachedScenicReviews(key: string): PageResult<ReviewItem> | null {
  const entry = scenicReviewsCache.get(key);
  if (!entry) {
    return null;
  }
  if (Date.now() > entry.expireAt) {
    scenicReviewsCache.delete(key);
    return null;
  }
  return cloneReviewPage(entry.data);
}

function setCachedScenicReviews(key: string, data: PageResult<ReviewItem>): void {
  scenicReviewsCache.set(key, {
    data: cloneReviewPage(data),
    expireAt: Date.now() + CACHE_TTL_MS,
  });

  while (scenicReviewsCache.size > CACHE_MAX_ENTRIES) {
    const oldestKey = scenicReviewsCache.keys().next().value as string | undefined;
    if (!oldestKey) {
      break;
    }
    scenicReviewsCache.delete(oldestKey);
  }
}

/**
 * 清除景点评论缓存。当用户提交新的评论或者删除评论后，需要调用此方法清除相关缓存，以确保后续获取评论列表时能够获取到最新的数据。
 */
export function clearScenicReviewsCache(scenicId?: number): void {
  if (scenicId === undefined) {
    scenicReviewsCache.clear();
    return;
  }
  const prefix = `${scenicId}:`;
  for (const key of scenicReviewsCache.keys()) {
    if (key.startsWith(prefix)) {
      scenicReviewsCache.delete(key);
    }
  }
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
  return http
    .get<PageResult<ReviewItem>>("/api/user/reviews/me", { params: query })
    .then(normalizeReviewPage);
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
  const cacheKey = buildScenicReviewsCacheKey(scenicId, query);
  const cached = getCachedScenicReviews(cacheKey);
  if (cached) {
    return Promise.resolve(cached);
  }

  return http
    .get<PageResult<ReviewItem>>(`/api/user/scenic-spots/${scenicId}/reviews`, { params: query })
    .then((res) => {
      const normalized = normalizeReviewPage(res);
      setCachedScenicReviews(cacheKey, normalized);
      return cloneReviewPage(normalized);
    });
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
  return http.post<number>("/api/user/reviews", payload).then((id) => {
    clearScenicReviewsCache(payload.scenicId);
    return id;
  });
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
