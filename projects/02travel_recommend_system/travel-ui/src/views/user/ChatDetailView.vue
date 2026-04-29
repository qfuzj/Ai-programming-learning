<!-- 极简风格AI对话详情页 -->
<template>
  <div class="page">
    <div class="header">
      <button class="btn-back" @click="router.push('/ai/chat')">← 返回</button>
      <h1 class="title">{{ title }}</h1>
    </div>

    <div class="chat-box">
      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="messages.length === 0" class="empty">暂无消息，开始对话吧</div>

      <div v-else class="message-list">
        <div v-for="msg in messages" :key="msg.messageId" class="message" :class="msg.role">
          <div class="avatar">{{ msg.role === "user" ? "我" : "AI" }}</div>
          <div class="bubble">{{ msg.content }}</div>
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
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getConversationDetail,
  getConversationMessages,
  sendConversationMessage,
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
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function send(): Promise<void> {
  const content = text.value.trim();
  if (!content || !conversationId.value) return;
  sending.value = true;
  try {
    const res = await sendConversationMessage(conversationId.value, content);
    messages.value = [
      ...messages.value,
      buildMsg(res.userMessageId, "user", content),
      buildMsg(res.assistantMessageId, "assistant", res.replyContent),
    ];
    text.value = "";
  } catch {
    alert("发送失败");
  } finally {
    sending.value = false;
  }
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

watch(
  conversationId,
  () => {
    if (conversationId.value) loadData();
  },
  { immediate: true }
);
onMounted(() => {
  if (conversationId.value) loadData();
});
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.btn-back {
  padding: 6px 12px;
  font-size: 14px;
  color: #666666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #000000;
  color: #000000;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin: 0;
}

.chat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
  border-radius: 16px;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50%;
  flex-shrink: 0;
}

.message.user .avatar {
  background: #00e676;
  color: #000000;
}

.message.assistant .avatar {
  background: #e0e0e0;
  color: #000000;
}

.bubble {
  max-width: 70%;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  border-radius: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .bubble {
  background: #00e676;
  color: #000000;
  border-bottom-right-radius: 4px;
}

.message.assistant .bubble {
  background: #ffffff;
  color: #000000;
  border: 1px solid #f0f0f0;
  border-bottom-left-radius: 4px;
}

.input-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  align-items: flex-end;
}

.input {
  flex: 1;
  padding: 10px 14px;
  font-size: 14px;
  color: #000000;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 12px;
  outline: none;
  resize: none;
  font-family: inherit;
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
  background: #00e676;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-send:hover:not(:disabled) {
  background: #00c665;
}

.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading,
.empty {
  flex: 1;
  display: grid;
  place-items: center;
  color: #999999;
  font-size: 14px;
}
</style>
