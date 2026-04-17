/**
 * 管理端用户管理接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-管理员用户详情 VO
 */
export interface AdminUserItem {
  id: number;
  username?: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
  gender?: number;
  birthday?: string;
  regionId?: number;
  regionName?: string;
  status?: number;
  lastLoginIp?: string;
  createdAt?: string;
  updatedAt?: string;
  lastLoginTime?: string;
}

/**
 * 对应-用户管理查询 DTO
 */
export interface AdminUserQuery extends PageQuery {
  keyword?: string;
  status?: number;
}

export function getAdminUserPage(query: AdminUserQuery): Promise<PageResult<AdminUserItem>> {
  return http.get("/api/admin/users", { params: query });
}

export function getAdminUserDetail(id: number): Promise<AdminUserItem> {
  return http.get(`/api/admin/users/${id}`);
}

export function updateAdminUserStatus(id: number, status: number): Promise<void> {
  return http.put(`/api/admin/users/${id}/status`, { status });
}
