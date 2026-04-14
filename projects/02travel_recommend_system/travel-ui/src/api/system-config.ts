/**
 * 管理端系统配置接口。
 */
import http from "@/api/http";
import type { PageResult } from "@/types/api";

export interface SystemConfigItem {
  id: number;
  configKey?: string;
  configValue?: string;
  description?: string;
  configType?: string;
  configGroup?: string;
  isPublic?: number;
  updateTime?: string;
}

export interface SystemConfigUpdatePayload {
  configValue: string;
  description?: string;
}

interface SystemConfigPageQuery {
  pageNum: number;
  pageSize: number;
}

interface SystemConfigPageResponse extends PageResult<SystemConfigItem> {}

export async function getSystemConfigList(): Promise<SystemConfigItem[]> {
  const pageQuery: SystemConfigPageQuery = { pageNum: 1, pageSize: 1000 };
  const page = await http.get<any, SystemConfigPageResponse>("/api/admin/system-configs", {
    params: pageQuery,
  });
  return page.records;
}

export function getSystemConfigByKey(key: string): Promise<SystemConfigItem> {
  return http.get(`/api/admin/system-configs/${key}`);
}

export async function updateSystemConfig(
  key: string,
  payload: SystemConfigUpdatePayload
): Promise<void> {
  const current = await getSystemConfigByKey(key);
  return http.put(`/api/admin/system-configs/${key}`, {
    configValue: payload.configValue,
    configType: current.configType ?? "string",
    configGroup: current.configGroup ?? "default",
    description: payload.description ?? current.description ?? "",
    isPublic: current.isPublic ?? 0,
  });
}
