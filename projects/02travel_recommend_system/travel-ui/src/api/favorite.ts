/**
 * 收藏模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface FavoriteItem {
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  score?: number;
  favoriteTime?: string;
}

export function getFavoritesPage(query: PageQuery): Promise<PageResult<FavoriteItem>> {
  return http.get("/api/user/favorites", { params: query });
}

export async function getFavorites(
  query: PageQuery = { pageNum: 1, pageSize: 10 }
): Promise<FavoriteItem[]> {
  const page = await getFavoritesPage(query);
  return page.records;
}

export function addFavorite(scenicId: number): Promise<void> {
  return http.post(`/api/user/favorites/${scenicId}`);
}

export function removeFavorite(scenicId: number): Promise<void> {
  return http.delete(`/api/user/favorites/${scenicId}`);
}

export function clearFavorites(): Promise<void> {
  return http.delete(`/api/user/favorites/clear`);
}
