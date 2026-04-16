/**
 * 浏览历史模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-用户浏览历史记录VO
 */
export interface BrowseHistoryItem {
  id: number;
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  stayDuration?: number;
  source?: string;
  deviceType?: string;
  // Back-end has jackson.write-dates-as-timestamps=false, so LocalDateTime is serialized as ISO string.
  browseTime?: string;
}

/**
 * 上报浏览历史
 */
export function reportBrowseHistory(payload: {
  scenicId: number;
  stayDuration?: number;
  source?: string;
  deviceType?: string;
}): Promise<void> {
  return http.post<void>("/api/user/browse-history", payload);
}

/**
 * 获取浏览历史分页列表
 */
export function getBrowseHistoryPage(query: PageQuery): Promise<PageResult<BrowseHistoryItem>> {
  return http.get<PageResult<BrowseHistoryItem>>("/api/user/browse-history", { params: query });
}

/**
 * 删除浏览历史
 */
export function deleteBrowseHistory(id: number): Promise<void> {
  return http.delete<void>(`/api/user/browse-history/${id}`);
}

/**
 * 清空浏览历史
 */
export function clearBrowseHistory(): Promise<void> {
  return http.delete<void>("/api/user/browse-history/clear");
}
