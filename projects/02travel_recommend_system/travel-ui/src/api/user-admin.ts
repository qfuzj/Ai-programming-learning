/**
 * 管理端用户管理接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface AdminUserItem {
  id: number;
  username?: string;
  nickname?: string;
  email?: string;
  phone?: string;
  avatar?: string;
  gender?: number;
  birthday?: string;
  regionId?: number;
  regionName?: string;
  role?: string;
  status?: number;
  lastLoginIp?: string;
  createTime?: string;
  updateTime?: string;
  lastLoginTime?: string;
}

export interface AdminUserQuery extends PageQuery {
  keyword?: string;
  status?: number;
}

export function getAdminUserPage(
  query: AdminUserQuery
): Promise<PageResult<AdminUserItem>> {
  return http.get("/api/admin/users", { params: query });
}

export function getAdminUserDetail(id: number): Promise<AdminUserItem> {
  return http.get(`/api/admin/users/${id}`);
}

export function updateAdminUserStatus(id: number, status: number): Promise<void> {
  return http.put(`/api/admin/users/${id}/status`, { status });
}
