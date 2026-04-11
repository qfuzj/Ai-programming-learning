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
