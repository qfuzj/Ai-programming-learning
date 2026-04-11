/**
 * 浏览历史模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface BrowseHistoryItem {
  id: number;
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  stayDuration?: number;
  source?: string;
  deviceType?: string;
  browseTime?: string;
}

export function reportBrowseHistory(payload: {
  scenicId: number;
  stayDuration?: number;
  source?: string;
}): Promise<void> {
  return http.post("/api/user/browse-history", payload);
}

export function getBrowseHistoryPage(query: PageQuery): Promise<PageResult<BrowseHistoryItem>> {
  return http.get("/api/user/browse-history", { params: query });
}

export async function getBrowseHistoryList(
  query: PageQuery = { pageNum: 1, pageSize: 10 }
): Promise<BrowseHistoryItem[]> {
  const page = await getBrowseHistoryPage(query);
  return page.records;
}

export function deleteBrowseHistory(id: number): Promise<void> {
  return http.delete(`/api/user/browse-history/${id}`);
}

export function clearBrowseHistory(): Promise<void> {
  return http.delete("/api/user/browse-history/clear");
}
