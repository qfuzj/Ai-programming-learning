<!-- 极简风格AI对话详情页 -->
<template>
  <div class="page">
    <div class="header">
      <button class="btn-back" @click="router.push('/ai/chat')">← 返回</button>
      <h1 class="title">{{ title }}</h1>
    </div>

    <div class="chat-box">
      <div v-if="loading" class="loading">加载中...</div>

      <div v-else ref="messageListRef" class="message-list">
        <div v-if="messages.length === 0" class="empty-inline">暂无消息，开始对话吧</div>
        <div v-for="msg in messages" :key="msg.messageId" class="message" :class="msg.role">
          <div class="avatar">{{ msg.role === "user" ? "我" : "AI" }}</div>
          <div class="bubble">
            {{ msg.content }}
          </div>
        </div>
      </div>

      <div class="input-bar">
        <textarea
          v-model="text"
          class="input"
          placeholder="输入消息..."
          rows="2"
          @keyup.enter.ctrl="send"
        ></textarea>
        <button class="btn-send" :disabled="sending || !text.trim()" @click="send">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  getConversationDetail,
  getConversationMessages,
  sendConversationMessageStream,
  type ChatMessageItem,
  ContentType,
} from "@/api/conversation";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const sending = ref(false);
const messages = ref<ChatMessageItem[]>([]);
const text = ref("");
const title = ref("会话详情");
const DEFAULT_TITLES = new Set(["新的旅行咨询", "新对话", "新的对话", "默认会话", "会话详情"]);
const messageListRef = ref<HTMLElement>();
let titlePollingToken = 0;

const conversationId = computed(() => Number(route.params.conversationId));

async function loadData(): Promise<void> {
  const id = conversationId.value;
  if (!id) {
    messages.value = [];
    return;
  }
  loading.value = true;
  try {
    const [detail, msgs] = await Promise.all([
      getConversationDetail(id),
      getConversationMessages(id),
    ]);
    title.value = detail.title || `会话 #${id}`;
    messages.value = msgs || [];
    await nextTick();
    scrollToBottom();
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadConversationTitle(id: number): Promise<string> {
  const detail = await getConversationDetail(id);
  const nextTitle = detail.title || `会话 #${id}`;
  title.value = nextTitle;
  return nextTitle;
}

function startTitlePolling(id: number): void {
  titlePollingToken += 1;
  const currentToken = titlePollingToken;
  const maxAttempts = 12;
  const intervalMs = 1000;

  const poll = async (attempt: number): Promise<void> => {
    if (currentToken !== titlePollingToken || conversationId.value !== id) {
      return;
    }

    try {
      const nextTitle = await loadConversationTitle(id);
      if (!DEFAULT_TITLES.has(nextTitle)) {
        return;
      }
    } catch (error) {
      console.error("标题轮询失败:", error);
    }

    if (
      attempt >= maxAttempts ||
      currentToken !== titlePollingToken ||
      conversationId.value !== id
    ) {
      return;
    }

    window.setTimeout(() => {
      void poll(attempt + 1);
    }, intervalMs);
  };

  void poll(1);
}

async function syncConversationAfterSend(): Promise<void> {
  await loadData();
}

async function send(): Promise<void> {
  const content = text.value.trim();
  const id = conversationId.value;
  if (!content || !id) return;

  sending.value = true;
  text.value = "";

  // 立即显示用户消息
  const userMsg = buildMsg(Date.now(), "user", content);
  messages.value.push(userMsg);
  await nextTick();
  scrollToBottom();

  // 创建AI消息占位符
  const aiMsg = buildMsg(Date.now() + 1, "assistant", "");
  const aiMsgIndex = messages.value.length;
  messages.value.push(aiMsg);
  await nextTick();
  scrollToBottom();

  let pendingText = "";
  let flushTimer: number | null = null;

  const flushPending = (): void => {
    if (!pendingText) {
      return;
    }
    const target = messages.value[aiMsgIndex];
    if (!target) {
      pendingText = "";
      return;
    }
    target.content += pendingText;
    pendingText = "";
    nextTick(() => scrollToBottom());
  };

  const scheduleFlush = (): void => {
    if (flushTimer !== null) {
      return;
    }
    flushTimer = window.setTimeout(() => {
      flushTimer = null;
      flushPending();
    }, 50);
  };

  const clearFlushTimer = (): void => {
    if (flushTimer !== null) {
      window.clearTimeout(flushTimer);
      flushTimer = null;
    }
  };

  startTitlePolling(id);

  // 流式接收AI回复
  sendConversationMessageStream(
    id,
    content,
    (chunk: string) => {
      pendingText += chunk;
      scheduleFlush();
    },
    () => {
      clearFlushTimer();
      flushPending();
      sending.value = false;
      void syncConversationAfterSend();
    },
    (error: Error) => {
      clearFlushTimer();
      sending.value = false;
      console.error("发送失败:", error);
      ElMessage.error("发送失败，请重试");
      messages.value = messages.value.filter((m) => m !== userMsg && m !== aiMsg);
    }
  );
}

function buildMsg(id: number, role: "user" | "assistant", content: string): ChatMessageItem {
  return {
    messageId: id,
    role,
    content,
    contentType: ContentType.TEXT,
    tokensUsed: 0,
    llmCallLogId: 0,
    createdAt: new Date().toISOString(),
  };
}

function scrollToBottom(): void {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
}

watch(
  conversationId,
  () => {
    titlePollingToken += 1;
    if (conversationId.value) loadData();
  },
  { immediate: true }
);
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  max-width: 900px;
  height: calc(100vh - 64px);
  padding: 24px;
  margin: 0 auto;
}

.header {
  display: flex;
  flex-shrink: 0;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.btn-back {
  padding: 6px 12px;
  font-size: 14px;
  color: #666666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-back:hover {
  color: #000000;
  border-color: #000000;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #000000;
}

.chat-box {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: hidden;
  background: #f9f9f9;
  border-radius: 16px;
}

.message-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  overflow-y: auto;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 32px;
  height: 32px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50%;
}

.message.user .avatar {
  color: #000000;
  background: #00e676;
}

.message.assistant .avatar {
  color: #000000;
  background: #e0e0e0;
}

.bubble {
  max-width: 70%;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
  border-radius: 12px;
}

.message.user .bubble {
  color: #000000;
  background: #00e676;
  border-bottom-right-radius: 4px;
}

.message.assistant .bubble {
  color: #000000;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-bottom-left-radius: 4px;
}

.input-bar {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 16px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
}

.input {
  flex: 1;
  padding: 10px 14px;
  font-family: inherit;
  font-size: 14px;
  color: #000000;
  resize: none;
  outline: none;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 12px;
  transition: all 0.2s;
}

.input:focus {
  background: #ffffff;
  border-color: #00e676;
}

.btn-send {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 12px;
  transition: background 0.2s;
}

.btn-send:hover:not(:disabled) {
  background: #00c665;
}

.btn-send:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.loading,
.empty {
  display: grid;
  flex: 1;
  place-items: center;
  font-size: 14px;
  color: #999999;
}
</style>
