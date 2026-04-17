/**
 * 景点模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 前端-景点列表项 VO（View Object）
 * 注意：后端接口返回的字段可能不完全一致，需进行适当的数据规整化
 */
export interface ScenicItem {
  id: number;
  name: string;
  regionName: string;
  address?: string;
  longitude?: number;
  latitude?: number;
  coverImage?: string;
  openTime?: string;
  ticketPrice?: number;
  level?: string;
  category?: string;
  score: number;
  status?: number;
  tagList?: string[];
  isFavorite?: boolean;
}

/**
 * 前端-景点详细信息 VO
 */
export interface ScenicDetail extends ScenicItem {
  regionId?: number;
  description?: string;
  detailContent?: string;
  ticketInfo?: string;
  ratingCount?: number;
  viewCount?: number;
  favoriteCount?: number;
  reviewCount?: number;
  hotScore?: number;
  bestSeason?: string;
  suggestedHours?: number;
  tips?: string;
  sortOrder?: number;
  isRecommended?: number;
  tagIds?: number[];
  images?: Array<{ id: number; imageUrl: string; fileResourceId?: number }>;
}

/**
 * 前端-地区树形结构 VO
 */
export interface RegionTreeNode {
  id: number;
  parentId?: number;
  name: string;
  shortName?: string;
  level?: number;
  code?: string;
  longitude?: number;
  latitude?: number;
  isHot?: number;
  children?: RegionTreeNode[];
}

/**
 * 对应-景点筛选条件 VO
 */
export interface ScenicFilterOptions {
  regions: RegionTreeNode[];
  categories: string[];
  levels: string[];
}

/**
 * 对应-景点查询 DTO
 */
export interface ScenicQuery extends PageQuery {
  keyword?: string;
  regionId?: number;
  category?: string;
  level?: string;
  minScore?: number;
  sortBy?: "hot" | "score" | "createdAt";
  sortOrder?: "ASC" | "DESC";
  status?: number;
}

/**
 * 对应-景点创建 DTO
 */
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
  bestSeason?: string;
  suggestedHours?: number;
  tips?: string;
  status?: number;
  sortOrder?: number;
  isRecommended?: number;
  tagIds?: number[];
  imageIds?: number[];
}

/**
 * 对应-景点更新 DTO
 */
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

/**
 * 对应-景点图片创建 DTO
 */
export interface ScenicImageCreatePayload {
  fileResourceId?: number;
  imageUrl?: string;
  imageType?: number;
  title?: string;
  sortOrder?: number;
  isCover?: number;
}

/**
 * 对应-景点列表项 VO
 */
interface ScenicItemRaw {
  scenicId?: number;
  name: string;
  coverImage?: string;
  regionName?: string;
  category?: string;
  longitude?: number;
  latitude?: number;
  level?: string;
  status?: number;
  address?: string;
  openTime?: string;
  ticketPrice?: number;
  tagList?: string[];
  isFavorite?: boolean;
  score?: number;
}

/**
 * 对应-景点详细信息 VO
 * 注意：后端接口返回的字段可能不完全一致，需进行适当的数据规整化
 */
interface ScenicDetailRaw extends ScenicItemRaw {
  description?: string;
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
  tagIds?: number[];
  images?: ScenicImageItem[];
}

/**
 * 数据规整化
 */
function normalizeScenicItem(raw: ScenicItemRaw): ScenicItem {
  const resolvedId = Number(raw.scenicId ?? 0);
  const resolvedScore = Number(raw.score ?? 0);
  return {
    id: resolvedId,
    name: raw.name,
    coverImage: raw.coverImage,
    regionName: raw.regionName ?? "",
    score: Number.isFinite(resolvedScore) ? resolvedScore : 0,
    category: raw.category,
    longitude: raw.longitude,
    latitude: raw.latitude,
    level: raw.level,
    status: raw.status,
    address: raw.address,
    openTime: raw.openTime,
    ticketPrice: raw.ticketPrice,
    tagList: raw.tagList ?? [],
    isFavorite: !!raw.isFavorite,
  };
}

/**
 * 数据规整化
 */
function normalizeScenicDetail(raw: ScenicDetailRaw): ScenicDetail {
  const base = normalizeScenicItem(raw);
  return {
    ...base,
    description: raw.description,
    regionId: raw.regionId,
    favoriteCount: raw.favoriteCount,
    reviewCount: raw.reviewCount,
    hotScore: raw.hotScore,
    detailContent: raw.detailContent,
    ticketInfo: raw.ticketInfo,
    ratingCount: raw.ratingCount,
    viewCount: raw.viewCount,
    bestSeason: raw.bestSeason,
    suggestedHours: raw.suggestedHours,
    tips: raw.tips,
    sortOrder: raw.sortOrder,
    isRecommended: raw.isRecommended,
    tagIds: raw.tagIds,
    images: raw.images?.map((image) => ({
      id: image.id,
      imageUrl: image.imageUrl ?? "",
      fileResourceId: image.fileResourceId,
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
    .get<PageResult<ScenicItemRaw>>("/api/user/scenic-spots", { params: query })
    .then((res) => mapPageResult(res, normalizeScenicItem));
}

export function getScenicDetail(id: number): Promise<ScenicDetail> {
  return http
    .get<ScenicDetailRaw>(`/api/user/scenic-spots/${id}`)
    .then((res) => normalizeScenicDetail(res));
}

export function getAdminScenicDetail(id: number): Promise<ScenicDetail> {
  return http
    .get<ScenicDetailRaw>(`/api/admin/scenic-spots/${id}`)
    .then((res) => normalizeScenicDetail(res));
}

export function getScenicHotList(): Promise<ScenicItem[]> {
  return http
    .get<ScenicItemRaw[]>("/api/user/scenic-spots/hot")
    .then((res) => res.map(normalizeScenicItem));
}

export function getScenicFilterOptions(): Promise<ScenicFilterOptions> {
  return http.get<ScenicFilterOptions>("/api/user/scenic-spots/filter-options");
}

export function getAdminScenicPage(query: ScenicQuery): Promise<PageResult<ScenicItem>> {
  return http
    .get<PageResult<ScenicItemRaw>>("/api/admin/scenic-spots", { params: query })
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
