/**
 * 字典接口。
 */
import http from "@/api/http";

export interface DictItem {
  code: string | number;
  desc: string;
}

export function getUserReviewStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/user-review-status");
}

export function getSensitiveStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/sensitive-status");
}

export function getRecommendTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/recommend-type");
}

export function getLlmCallLogStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/llm-call-log-status");
}

export function getContentAuditStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/content-audit-status");
}

export function getConversationTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/conversation-type");
}

export function getConversationStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/conversation-status");
}

export function getMessageContentTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/message-content-type");
}

export function getBizTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/biz-type");
}

export function getFileResourceStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/file-resource-status");
}

export function getMessageRoleDict(): Promise<DictItem[]> {
  return http.get("/api/dict/message-roles");
}

export function getTravelPlanItemTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/travel-plan-item-type");
}

export function getTravelCompanionTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/travel-companion-type");
}

export function getTravelStyleDict(): Promise<DictItem[]> {
  return http.get("/api/dict/travel-style");
}

export function getPublicStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/public-status");
}

export function getConfigGroupDict(): Promise<DictItem[]> {
  return http.get("/api/dict/config-group");
}

export function getConfigTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/config-type");
}

export function getOperationLogModuleDict(): Promise<DictItem[]> {
  return http.get("/api/dict/operation-log-module");
}

export function getOperationLogStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/operation-log-status");
}

export function getFileResourceUploaderTypeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/file-resource-uploader-type");
}

export function getGenderDict(): Promise<DictItem[]> {
  return http.get("/api/dict/gender");
}

export function getCommonStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/common-status");
}

export function getTagScopeDict(): Promise<DictItem[]> {
  return http.get("/api/dict/tag-scope");
}

export function getRegionLevelDict(): Promise<DictItem[]> {
  return http.get("/api/dict/region-level");
}

export function getTravelPlanStatusDict(): Promise<DictItem[]> {
  return http.get("/api/dict/travel-plan-status");
}

export function getYesNoFlagDict(): Promise<DictItem[]> {
  return http.get("/api/dict/yes-no-flag");
}
