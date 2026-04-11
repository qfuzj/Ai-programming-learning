/**
 * 管理端系统配置接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface SystemConfigItem {
  id: number;
  configKey: string;
  configValue: string;
  configType: string;
  configGroup: string;
  description?: string;
  isPublic: number;
  createTime?: string;
  updateTime?: string;
}

export interface SystemConfigQuery extends PageQuery {
  configGroup?: string;
}

export interface SystemConfigUpdatePayload {
  configValue: string;
  configType: string;
  configGroup: string;
  description?: string;
  isPublic: number;
}

export function getSystemConfigPage(
  query: SystemConfigQuery
): Promise<PageResult<SystemConfigItem>> {
  return http.get("/api/admin/system-configs", { params: query });
}

export function getSystemConfigDetail(configKey: string): Promise<SystemConfigItem> {
  return http.get(`/api/admin/system-configs/${configKey}`);
}

export function updateSystemConfig(
  configKey: string,
  payload: SystemConfigUpdatePayload
): Promise<void> {
  return http.put(`/api/admin/system-configs/${configKey}`, payload);
}
