/**
 * 管理端操作日志接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface OperationLogItem {
  id: number;
  adminUserId?: number;
  adminUsername?: string;
  module?: string;
  action?: string;
  description?: string;
  requestMethod?: string;
  requestUrl?: string;
  requestParams?: string;
  responseData?: string;
  ipAddress?: string;
  userAgent?: string;
  executionTimeMs?: number;
  status?: number;
  errorMessage?: string;
  createTime?: string;
}

export interface OperationLogQuery extends PageQuery {
  module?: string;
  action?: string;
  status?: number;
  adminUsername?: string;
}

export function getOperationLogPage(
  query: OperationLogQuery
): Promise<PageResult<OperationLogItem>> {
  return http.get("/api/admin/operation-logs", { params: query });
}

export function getOperationLogDetail(id: number): Promise<OperationLogItem> {
  return http.get(`/api/admin/operation-logs/${id}`);
}
