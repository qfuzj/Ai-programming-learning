/**
 * 会话与对话模块接口。
 */
import http from "@/api/http";

// 会话类型枚举
export enum ConversationType {
  SMART_SERVICE = 1, // 智能客服
  TRIP_PLANNING = 2, // 行程规划
  ATTRACTION_CONSULT = 3, // 景点咨询
}

// 会话状态
export enum ConversationStatus {
  CLOSED = 0, // 关闭
  ACTIVE = 1, // 进行中
}

// 消息内容类型
export enum ContentType {
  TEXT = 1, // 文本
  IMAGE = 2, // 图片
}

// 消息角色
export type MessageRole = "user" | "assistant" | "system";

/**
 * 对应-LLM会话VO类
 */
export interface ConversationItem {
  conversationId: number;
  title: string;
  conversationType: number;
  messageCount: number;
  totalTokens: number;
  status: number;
  // 后端返回 LocalDateTime，前端以 ISO 8601 字符串格式接收
  createdAt: string;
  // 后端返回 LocalDateTime，前端以 ISO 8601 字符串格式接收
  updatedAt: string;
}

/**
 * 对应-LLM消息VO类
 */
export interface ChatMessageItem {
  messageId: number;
  // 使用 string 防御后端可能返回的其他值，但主要类型期望为 MessageRole
  role: string | MessageRole;
  content: string;
  contentType: number;
  tokensUsed: number;
  llmCallLogId: number;
  // 后端返回 LocalDateTime，前端以 ISO 8601 字符串格式接收
  createdAt: string;
}

/**
 * 对应-发送消息接口响应DTO
 */
export interface SendConversationMessageResponse {
  userMessageId: number;
  assistantMessageId: number;
  replyContent: string;
  tokenUsage: number;
  modelName: string;
}

// 对应-创建聊天会话DTO
export interface CreateConversationPayload {
  title: string;
  conversationType: number;
}

/**
 * 获取会话列表
 */
export function getConversations(): Promise<ConversationItem[]> {
  return http.get<ConversationItem[]>("/api/user/chat/conversations");
}

/**
 * 获取会话详情
 */
export function getConversationDetail(conversationId: number): Promise<ConversationItem> {
  return http.get<ConversationItem>(`/api/user/chat/conversations/${conversationId}`);
}

/**
 * 创建聊天会话
 */
export function createConversation(
  payload: CreateConversationPayload
): Promise<{ conversationId: number }> {
  return http.post<{ conversationId: number }>("/api/user/chat/conversations", {
    title: payload.title,
    conversationType: payload.conversationType,
  });
}

/**
 * 获取会话消息
 */
export function getConversationMessages(conversationId: number): Promise<ChatMessageItem[]> {
  return http.get<ChatMessageItem[]>(`/api/user/chat/conversations/${conversationId}/messages`);
}

/**
 * 发送会话消息
 */
export function sendConversationMessage(
  conversationId: number,
  content: string
): Promise<SendConversationMessageResponse> {
  // 注意：后端目前 DTO 不支持 contextScenicId 参数，已将其删除
  return http.post<SendConversationMessageResponse>(
    `/api/user/chat/conversations/${conversationId}/messages`,
    {
      content,
    }
  );
}

/**
 * 删除会话
 */
export function deleteConversation(conversationId: number): Promise<void> {
  return http.delete<void>(`/api/user/chat/conversations/${conversationId}`);
}
