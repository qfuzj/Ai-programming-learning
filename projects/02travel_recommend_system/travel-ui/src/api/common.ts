/**
 * 公共基础接口：地区、标签等。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface CommonRegionNode {
  id: number;
  name: string;
  children?: CommonRegionNode[];
}

export interface CommonTagItem {
  id: number;
  name: string;
  type?: number;
  category?: string;
  icon?: string;
  color?: string;
  sortOrder?: number;
  status?: number;
}

export interface AdminRegionItem {
  id: number;
  parentId: number;
  name: string;
  shortName?: string;
  level: number;
  code?: string;
  pinyin?: string;
  longitude?: number;
  latitude?: number;
  sortOrder?: number;
  isHot?: number;
}

export interface RegionPayload {
  parentId: number;
  name: string;
  shortName?: string;
  level: number;
  code?: string;
  pinyin?: string;
  longitude?: number;
  latitude?: number;
  sortOrder?: number;
  isHot?: number;
}

export interface AdminRegionQuery extends PageQuery {
  parentId?: number;
  name?: string;
  level?: number;
  code?: string;
  isHot?: number;
}

export interface AdminTagItem {
  id: number;
  name: string;
  type: number;
  category?: string;
  icon?: string;
  color?: string;
  sortOrder?: number;
  status?: number;
}

export interface TagPayload {
  name: string;
  type?: number;
  category?: string;
  icon?: string;
  color?: string;
  sortOrder?: number;
  status?: number;
}

export interface AdminTagQuery extends PageQuery {
  name?: string;
  type?: number;
  category?: string;
  status?: number;
}

export function getRegionTree(): Promise<CommonRegionNode[]> {
  return http.get("/api/common/regions/tree");
}

export function getTags(): Promise<CommonTagItem[]> {
  return http.get("/api/common/tags");
}

export async function getTagsByType(type: number | string): Promise<CommonTagItem[]> {
  const normalizedType = Number(type);

  if (!Number.isNaN(normalizedType)) {
    return http.get("/api/common/tags", { params: { type: normalizedType } });
  }

  const tags = await getTags();
  return tags.filter((tag) => tag.category === type);
}

export function getAdminRegionPage(query: AdminRegionQuery): Promise<PageResult<AdminRegionItem>> {
  return http.get("/api/admin/regions", { params: query });
}

export function createAdminRegion(payload: RegionPayload): Promise<number> {
  return http.post("/api/admin/regions", payload);
}

export function updateAdminRegion(id: number, payload: RegionPayload): Promise<void> {
  return http.put(`/api/admin/regions/${id}`, payload);
}

export function deleteAdminRegion(id: number): Promise<void> {
  return http.delete(`/api/admin/regions/${id}`);
}

export function getAdminTagPage(query: AdminTagQuery): Promise<PageResult<AdminTagItem>> {
  return http.get("/api/admin/tags", { params: query });
}

export function createAdminTag(payload: TagPayload): Promise<number> {
  return http.post("/api/admin/tags", payload);
}

export function updateAdminTag(id: number, payload: TagPayload): Promise<void> {
  return http.put(`/api/admin/tags/${id}`, payload);
}

export function deleteAdminTag(id: number): Promise<void> {
  return http.delete(`/api/admin/tags/${id}`);
}
