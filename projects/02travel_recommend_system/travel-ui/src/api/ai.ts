/**
 * AI 能力接口：推荐与对话。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";
import {
  createConversation,
  deleteConversation,
  getConversationDetail,
  getConversationMessages,
  getConversations,
  sendConversationMessage,
} from "@/api/chat";
import type {
  ChatMessageItem,
  ConversationItem,
  SendConversationMessageResponse,
} from "@/api/chat";

export interface AiRecommendItem {
  recommendRecordId?: number;
  resultItemId?: number;
  scenicId: number;
  scenicName: string;
  reason?: string;
  coverImage?: string;
  score?: number;
  sourceType?: string;
  rankScore?: number;
}

export interface AiChatMessage {
  role: "user" | "assistant";
  content: string;
}

export type AiConversationItem = ConversationItem;
export type AiConversationMessage = ChatMessageItem;
export type AiSendMessageResponse = SendConversationMessageResponse;

export interface RecommendFeedbackPayload {
  recommendRecordId: number;
  resultItemId: number;
  scenicId: number;
}

interface RecommendItemRaw {
  recommendRecordId?: number;
  resultItemId?: number;
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  score?: number;
  recommendReason?: string;
  sourceType?: string;
  rankScore?: number;
}

function mapRecommendItem(raw: RecommendItemRaw): AiRecommendItem {
  return {
    recommendRecordId: raw.recommendRecordId,
    resultItemId: raw.resultItemId,
    scenicId: raw.scenicId,
    scenicName: raw.scenicName,
    coverImage: raw.coverImage,
    score: raw.score,
    reason: raw.recommendReason,
    sourceType: raw.sourceType,
    rankScore: raw.rankScore,
  };
}

function mapRecommendPage(page: PageResult<RecommendItemRaw>): PageResult<AiRecommendItem> {
  return {
    ...page,
    records: page.records.map(mapRecommendItem),
  };
}

export function fetchAiRecommendations(
  query: Partial<PageQuery> = { pageNum: 1, pageSize: 10 }
): Promise<PageResult<AiRecommendItem>> {
  return http
    .get<any, PageResult<RecommendItemRaw>>("/api/user/recommend/home", { params: query })
    .then(mapRecommendPage);
}

export function fetchSimilarRecommendations(
  scenicId: number,
  query: Partial<PageQuery> = { pageNum: 1, pageSize: 10 }
): Promise<PageResult<AiRecommendItem>> {
  return http
    .get<any, PageResult<RecommendItemRaw>>(`/api/user/recommend/scenic/${scenicId}`, {
      params: query,
    })
    .then(mapRecommendPage);
}

export function sendRecommendExposure(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post("/api/user/recommend/feedback/exposure", payload);
}

export function sendRecommendClick(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post("/api/user/recommend/feedback/click", payload);
}

export function sendRecommendFavorite(payload: RecommendFeedbackPayload): Promise<void> {
  return http.post("/api/user/recommend/feedback/favorite", payload);
}

export function listAiConversations(): Promise<AiConversationItem[]> {
  return getConversations();
}

export function getAiConversationDetail(conversationId: number): Promise<AiConversationItem> {
  return getConversationDetail(conversationId);
}

export function getAiConversationMessages(
  conversationId: number
): Promise<AiConversationMessage[]> {
  return getConversationMessages(conversationId);
}

export function createAiConversation(
  title: string,
  scene = "travel_consult"
): Promise<{ conversationId: number }> {
  return createConversation({
    title,
    scene: scene as "customer_service" | "travel_plan" | "travel_consult",
  });
}

export function sendAiChatMessage(
  conversationId: number,
  message: string,
  contextScenicId?: number
): Promise<AiSendMessageResponse> {
  return sendConversationMessage(conversationId, message, contextScenicId);
}

export function deleteAiConversation(conversationId: number): Promise<void> {
  return deleteConversation(conversationId);
}
