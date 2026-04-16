/**
 * 管理端操作日志接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-操作日志列表 VO
 */
export interface OperationLogListItem {
  id: number;
  module?: string;
  action?: string;
  adminUsername?: string;
  description?: string;
  requestUrl?: string;
  executionTimeMs?: number;
  status?: number;
  createdAt?: string;
}

/**
 * 对应-操作日志实体类
 */
export interface OperationLogDetailItem {
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
  createdAt?: string;
}

/**
 * 对应-操作日志查询DTO
 */
export interface OperationLogQuery extends PageQuery {
  module?: string;
  action?: string;
  status?: number;
  adminUsername?: string;
}

export function getOperationLogPage(
  query: OperationLogQuery
): Promise<PageResult<OperationLogListItem>> {
  return http.get("/api/admin/operation-logs", { params: query });
}

export function getOperationLogDetail(id: number): Promise<OperationLogDetailItem> {
  return http.get(`/api/admin/operation-logs/${id}`);
}
