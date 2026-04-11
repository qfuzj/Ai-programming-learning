/**
 * 会话与对话模块接口。
 */
import http from "@/api/http";

export interface ConversationItem {
  conversationId: number;
  title: string;
  updatedAt: string;
  conversationType?: string;
  status?: string;
  messageCount?: number;
  totalTokens?: number;
}

export interface ChatMessageItem {
  id: number;
  role: "user" | "assistant";
  content: string;
  contentType?: number;
  tokensUsed?: number;
  llmCallLogId?: number;
  createdAt?: string;
}

export interface SendConversationMessageResponse {
  userMessageId: number;
  assistantMessageId: number;
  replyContent: string;
  tokenUsage?: number;
  modelName?: string;
}

export interface CreateConversationPayload {
  title: string;
  conversationType?: number;
  scene?: "customer_service" | "travel_plan" | "travel_consult";
}

interface ConversationRaw {
  id: number;
  title: string;
  lastMessageAt?: string;
  conversationType?: string;
  status?: string;
  messageCount?: number;
  totalTokens?: number;
}

interface ChatMessageRaw {
  id: number;
  role: "user" | "assistant" | string;
  content: string;
  contentType?: number;
  tokensUsed?: number;
  llmCallLogId?: number;
  createTime?: string;
}

const SCENE_TO_TYPE: Record<string, number> = {
  customer_service: 1,
  travel_plan: 2,
  travel_consult: 3,
};

function mapConversation(raw: ConversationRaw): ConversationItem {
  return {
    conversationId: raw.id,
    title: raw.title,
    updatedAt: raw.lastMessageAt ?? "",
    conversationType: raw.conversationType,
    status: raw.status,
    messageCount: raw.messageCount,
    totalTokens: raw.totalTokens,
  };
}

function mapMessage(raw: ChatMessageRaw): ChatMessageItem {
  return {
    id: raw.id,
    role: raw.role === "assistant" ? "assistant" : "user",
    content: raw.content,
    contentType: raw.contentType,
    tokensUsed: raw.tokensUsed,
    llmCallLogId: raw.llmCallLogId,
    createdAt: raw.createTime,
  };
}

export function getConversations(): Promise<ConversationItem[]> {
  return http
    .get<any, ConversationRaw[]>("/api/user/chat/conversations")
    .then((res) => res.map(mapConversation));
}

export function getConversationDetail(conversationId: number): Promise<ConversationItem> {
  return http
    .get<any, ConversationRaw>(`/api/user/chat/conversations/${conversationId}`)
    .then(mapConversation);
}

export function createConversation(
  payload: CreateConversationPayload
): Promise<{ conversationId: number }> {
  const conversationType =
    payload.conversationType ?? (payload.scene ? SCENE_TO_TYPE[payload.scene] : undefined) ?? 3;

  return http
    .post<any, number>("/api/user/chat/conversations", {
      title: payload.title,
      conversationType,
    })
    .then((conversationId) => ({ conversationId }));
}

export function getConversationMessages(conversationId: number): Promise<ChatMessageItem[]> {
  return http
    .get<any, ChatMessageRaw[]>(`/api/user/chat/conversations/${conversationId}/messages`)
    .then((res) => res.map(mapMessage));
}

export function sendConversationMessage(
  conversationId: number,
  content: string,
  contextScenicId?: number
): Promise<SendConversationMessageResponse> {
  return http.post(`/api/user/chat/conversations/${conversationId}/messages`, {
    content,
    contextScenicId,
  });
}

export function deleteConversation(conversationId: number): Promise<void> {
  return http.delete(`/api/user/chat/conversations/${conversationId}`);
}
