/**
 * 管理端系统配置接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

/**
 * 对应-系统配置实体
 */
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

/**
 * 对应-系统配置查询 DTO
 */
export interface SystemConfigQuery extends PageQuery {
  keyword?: string;
  configGroup?: string;
}

/**
 * 对应-系统配置更新 DTO
 */
export interface SystemConfigUpdatePayload {
  configValue: string;
  configType: string;
  configGroup: string;
  description?: string;
  isPublic: number;
}

export function getSystemConfigList(
  query: SystemConfigQuery
): Promise<PageResult<SystemConfigItem>> {
  return http.get<PageResult<SystemConfigItem>>("/api/admin/system-configs", {
    params: query,
  });
}

export function getSystemConfigByKey(key: string): Promise<SystemConfigItem> {
  return http.get<SystemConfigItem>(`/api/admin/system-configs/${key}`);
}

export function updateSystemConfig(key: string, payload: SystemConfigUpdatePayload): Promise<void> {
  return http.put(`/api/admin/system-configs/${key}`, payload);
}
