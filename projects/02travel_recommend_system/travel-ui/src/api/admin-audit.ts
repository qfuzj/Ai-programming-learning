/**
 * 管理端内容审核接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface AuditItem {
  id: number;
  contentType?: string;
  contentId?: number;
  contentSnapshot?: string;
  submitUserId?: number;
  auditStatus?: number;
  autoAuditResult?: string;
  autoAuditScore?: number;
  llmCallLogId?: number;
  auditorId?: number;
  auditRemark?: string;
  auditTime?: string;
  violationType?: string;
  createTime?: string;
  updateTime?: string;
}

export interface AuditQuery extends PageQuery {
  contentType?: string;
  auditStatus?: number;
  contentId?: number;
  submitUserId?: number;
}

export interface AuditActionPayload {
  reason?: string;
}

export function getAuditPage(query: AuditQuery): Promise<PageResult<AuditItem>> {
  return http.get("/api/admin/audits", { params: query });
}

export function approveAudit(id: number, payload?: AuditActionPayload): Promise<void> {
  return http.post(`/api/admin/audits/${id}/approve`, payload ?? {});
}

export function rejectAudit(id: number, payload: AuditActionPayload): Promise<void> {
  return http.post(`/api/admin/audits/${id}/reject`, payload);
}

export function hideAudit(id: number, payload?: AuditActionPayload): Promise<void> {
  return http.post(`/api/admin/audits/${id}/hide`, payload ?? {});
}
