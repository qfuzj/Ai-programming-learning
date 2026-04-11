/**
 * 景点模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface ScenicItem {
  id: number;
  scenicId?: number;
  name: string;
  coverImage?: string;
  regionName: string;
  tags: string[];
  tagList?: string[];
  score: number;
  level?: string;
  status?: number;
  intro?: string;
  address?: string;
  openTime?: string;
  ticketPrice?: number;
}

export interface ScenicDetail extends ScenicItem {
  images?: Array<{ id: number; imageUrl: string }>;
  regionId?: number;
  favoriteCount?: number;
  reviewCount?: number;
  hotScore?: number;
  detailContent?: string;
  ticketInfo?: string;
  ratingScore?: number;
  ratingCount?: number;
  viewCount?: number;
  bestSeason?: string;
  suggestedHours?: number;
  tips?: string;
  sortOrder?: number;
  isRecommended?: number;
  isFavorite?: boolean;
}

export interface ScenicFilterOptions {
  regions: Array<{ id: number; name: string }>;
  categories: string[];
  levels: string[];
}

export interface ScenicQuery extends PageQuery {
  keyword?: string;
  regionId?: number;
  category?: string;
  level?: string;
  minScore?: number;
  sortBy?: "hot" | "score" | "createTime";
  status?: number;
}

export interface ScenicCreatePayload {
  name: string;
  regionId: number;
  address: string;
  longitude?: number;
  latitude?: number;
  coverImage?: string;
  description?: string;
  detailContent?: string;
  openTime?: string;
  ticketInfo?: string;
  ticketPrice?: number;
  level?: string;
  category?: string;
  ratingScore?: number;
  bestSeason?: string;
  suggestedHours?: number;
  tips?: string;
  status?: number;
  sortOrder?: number;
  isRecommended?: number;
  tagIds?: number[];
  imageIds?: number[];
}

export interface ScenicUpdatePayload extends Partial<ScenicCreatePayload> {}

export interface ScenicImageItem {
  id: number;
  fileResourceId?: number;
  imageUrl?: string;
  imageType?: number;
  title?: string;
  sortOrder?: number;
  isCover?: number;
}

export interface ScenicImageCreatePayload {
  fileResourceId?: number;
  imageUrl?: string;
  imageType?: number;
  title?: string;
  sortOrder?: number;
  isCover?: number;
}

interface ScenicItemRaw {
  scenicId?: number;
  name: string;
  coverImage?: string;
  regionName?: string;
  score?: number;
  ratingScore?: number;
  level?: string;
  status?: number;
  description?: string;
  address?: string;
  openTime?: string;
  ticketPrice?: number;
  tagList?: string[];
}

interface ScenicDetailRaw extends ScenicItemRaw {
  regionId?: number;
  favoriteCount?: number;
  reviewCount?: number;
  hotScore?: number;
  detailContent?: string;
  ticketInfo?: string;
  ratingCount?: number;
  viewCount?: number;
  bestSeason?: string;
  suggestedHours?: number;
  tips?: string;
  sortOrder?: number;
  isRecommended?: number;
  isFavorite?: boolean;
  images?: ScenicImageItem[];
}

function normalizeScenicItem(raw: ScenicItemRaw): ScenicItem {
  const resolvedId = Number(raw.scenicId ?? 0);
  const resolvedScore = Number(raw.score ?? raw.ratingScore ?? 0);
  return {
    id: resolvedId,
    scenicId: resolvedId,
    name: raw.name,
    coverImage: raw.coverImage,
    regionName: raw.regionName ?? "",
    tags: raw.tagList ?? [],
    tagList: raw.tagList ?? [],
    score: Number.isFinite(resolvedScore) ? resolvedScore : 0,
    level: raw.level,
    status: raw.status,
    intro: raw.description,
    address: raw.address,
    openTime: raw.openTime,
    ticketPrice: raw.ticketPrice,
  };
}

function normalizeScenicDetail(raw: ScenicDetailRaw): ScenicDetail {
  const base = normalizeScenicItem(raw);
  return {
    ...base,
    regionId: raw.regionId,
    favoriteCount: raw.favoriteCount,
    reviewCount: raw.reviewCount,
    hotScore: raw.hotScore,
    detailContent: raw.detailContent,
    ticketInfo: raw.ticketInfo,
    ratingScore: raw.ratingScore,
    ratingCount: raw.ratingCount,
    viewCount: raw.viewCount,
    bestSeason: raw.bestSeason,
    suggestedHours: raw.suggestedHours,
    tips: raw.tips,
    sortOrder: raw.sortOrder,
    isRecommended: raw.isRecommended,
    isFavorite: raw.isFavorite,
    images: raw.images?.map((image) => ({
      id: image.id,
      imageUrl: image.imageUrl ?? "",
    })),
  };
}

function mapPageResult<T, R>(page: PageResult<T>, mapper: (item: T) => R): PageResult<R> {
  return {
    ...page,
    records: page.records.map(mapper),
  };
}

export function getScenicPage(query: ScenicQuery): Promise<PageResult<ScenicItem>> {
  return http
    .get<any, PageResult<ScenicItemRaw>>("/api/user/scenic-spots", { params: query })
    .then((res) => mapPageResult(res, normalizeScenicItem));
}

export function getScenicDetail(id: number): Promise<ScenicDetail> {
  return http
    .get<any, ScenicDetailRaw>(`/api/user/scenic-spots/${id}`)
    .then((res) => normalizeScenicDetail(res));
}

export function getScenicHotList(): Promise<ScenicItem[]> {
  return http
    .get<any, ScenicItemRaw[]>("/api/user/scenic-spots/hot")
    .then((res) => res.map(normalizeScenicItem));
}

export function getScenicFilterOptions(): Promise<ScenicFilterOptions> {
  return http.get("/api/user/scenic-spots/filter-options");
}

export function getAdminScenicPage(query: ScenicQuery): Promise<PageResult<ScenicItem>> {
  return http
    .get<any, PageResult<ScenicItemRaw>>("/api/admin/scenic-spots", { params: query })
    .then((res) => mapPageResult(res, normalizeScenicItem));
}

export function createAdminScenic(payload: ScenicCreatePayload): Promise<number> {
  return http.post("/api/admin/scenic-spots", payload);
}

export function updateAdminScenic(id: number, payload: ScenicUpdatePayload): Promise<void> {
  return http.put(`/api/admin/scenic-spots/${id}`, payload);
}

export function deleteAdminScenic(id: number): Promise<void> {
  return http.delete(`/api/admin/scenic-spots/${id}`);
}

export function updateAdminScenicStatus(id: number, status: number): Promise<void> {
  return http.put(`/api/admin/scenic-spots/${id}/status`, { status });
}

export function batchUpdateAdminScenicStatus(scenicIds: number[], status: number): Promise<void> {
  return http.put("/api/admin/scenic-spots/batch/status", { scenicIds, status });
}

export function updateAdminScenicTags(id: number, tagIds: number[]): Promise<void> {
  return http.put(`/api/admin/scenic-spots/${id}/tags`, tagIds);
}

export function getAdminScenicImages(id: number): Promise<ScenicImageItem[]> {
  return http.get(`/api/admin/scenic-spots/${id}/images`);
}

export function addAdminScenicImage(
  id: number,
  payload: ScenicImageCreatePayload
): Promise<number> {
  return http.post(`/api/admin/scenic-spots/${id}/images`, payload);
}

export function deleteAdminScenicImage(id: number, imageId: number): Promise<void> {
  return http.delete(`/api/admin/scenic-spots/${id}/images/${imageId}`);
}
