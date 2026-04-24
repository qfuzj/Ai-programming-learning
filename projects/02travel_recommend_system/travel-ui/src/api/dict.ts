/**
 * 字典接口。
 */
import http from "@/api/http";

export interface DictItem {
  code: string | number;
  desc: string;
}

export function getUserReviewStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/user-review-status");
}

export function getSensitiveStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/sensitive-status");
}

export function getRecommendTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/recommend-type");
}

export function getLlmCallLogStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/llm-call-log-status");
}

export function getContentAuditStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/content-audit-status");
}

export function getConversationTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/conversation-type");
}

export function getConversationStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/conversation-status");
}

export function getMessageContentTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/message-content-type");
}

export function getBizTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/biz-type");
}

export function getFileResourceStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/file-resource-status");
}

export function getMessageRoleDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/message-roles");
}

export function getTravelPlanItemTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/travel-plan-item-type");
}

export function getTravelCompanionTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/travel-companion-type");
}

export function getTravelStyleDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/travel-style");
}

export function getPublicStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/public-status");
}

export function getConfigGroupDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/config-group");
}

export function getConfigTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/config-type");
}

export function getOperationLogModuleDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/operation-log-module");
}

export function getOperationLogStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/operation-log-status");
}

export function getFileResourceUploaderTypeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/file-resource-uploader-type");
}

export function getGenderDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/gender");
}

export function getCommonStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/common-status");
}

export function getTagScopeDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/tag-scope");
}

export function getRegionLevelDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/region-level");
}

export function getTravelPlanStatusDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/travel-plan-status");
}

export function getYesNoFlagDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/yes-no-flag");
}

export function getScenicLevelDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/scenic-level");
}

export function getScenicCategoryDict(): Promise<DictItem[]> {
  return http.get("/api/common/dict/scenic-category");
}
