<!-- AI 对话详情页：展示消息并发送新消息。 -->
<template>
  <PageStub :title="pageTitle" description="支持查看历史消息并继续对话。">
    <el-skeleton v-if="loading" animated :rows="6" />
    <template v-else>
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      <div v-else class="message-list">
        <div v-for="item in messages" :key="item.id" :class="['message-item', item.role]">
          <div class="message-role">{{ item.role === "user" ? "我" : "AI" }}</div>
          <div class="message-content">{{ item.content }}</div>
        </div>
      </div>

      <el-input v-model="message" type="textarea" :rows="4" placeholder="输入消息并发送" />
      <div class="action-row">
        <el-button :loading="sending" type="primary" @click="sendMessage">发送</el-button>
      </div>
    </template>
  </PageStub>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import {
  getConversationDetail,
  getConversationMessages,
  sendConversationMessage,
  type ChatMessageItem,
} from "@/api/chat";

const route = useRoute();
const conversationId = computed(() => Number(route.params.conversationId));
const pageTitle = ref("会话详情");
const loading = ref(false);
const sending = ref(false);
const messages = ref<ChatMessageItem[]>([]);
const message = ref("");

async function loadConversation(): Promise<void> {
  const id = conversationId.value;
  if (!id) {
    messages.value = [];
    return;
  }

  loading.value = true;
  try {
    const [detail, messageList] = await Promise.all([
      getConversationDetail(id),
      getConversationMessages(id),
    ]);
    pageTitle.value = detail.title || `会话详情 #${id}`;
    messages.value = messageList;
  } finally {
    loading.value = false;
  }
}

async function sendMessage(): Promise<void> {
  const content = message.value.trim();
  if (!content || !conversationId.value) {
    return;
  }

  sending.value = true;
  try {
    const response = await sendConversationMessage(conversationId.value, content);
    messages.value = [
      ...messages.value,
      { id: response.userMessageId, role: "user", content },
      { id: response.assistantMessageId, role: "assistant", content: response.replyContent },
    ];
    message.value = "";
  } finally {
    sending.value = false;
  }
}

watch(
  conversationId,
  () => {
    void loadConversation();
  },
  { immediate: true }
);
</script>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item {
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 12px;
}

.message-item.assistant {
  background: #eef2ff;
}

.message-role {
  margin-bottom: 6px;
  font-size: 12px;
  color: #6b7280;
}

.message-content {
  word-break: break-word;
  white-space: pre-wrap;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
