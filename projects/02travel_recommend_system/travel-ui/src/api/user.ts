/**
 * 管理端用户模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface AdminUserItem {
  id: number;
  username: string;
  nickname: string;
  status: number;
}

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
