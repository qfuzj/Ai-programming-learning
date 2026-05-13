<!-- 极简风格AI对话列表页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">AI 对话</h1>
      <button class="btn-new" @click="createNew">新建会话</button>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="n in 4" :key="n" class="skeleton-item">
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="list.length === 0" class="empty">
      <p>暂无对话记录</p>
      <button class="btn-link" @click="createNew">开始对话</button>
    </div>

    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.conversationId"
        class="list-item"
        @click="goDetail(item.conversationId)"
      >
        <div class="item-info">
          <h3 class="item-title">{{ item.title || "新对话" }}</h3>
          <p class="item-meta">
            <span>{{ item.messageCount || 0 }}条消息</span>
            <span class="dot">·</span>
            <span>{{ formatConversationType(item.conversationType) }}</span>
          </p>
        </div>
        <span class="item-time">{{ formatTime(item.updatedAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getConversations,
  createConversation,
  ConversationType,
  type ConversationItem,
} from "@/api/conversation";

const router = useRouter();
const loading = ref(false);
const creating = ref(false);
const list = ref<ConversationItem[]>([]);

function formatTime(time?: string): string {
  if (!time) return "";
  const d = new Date(time);
  return `${d.getMonth() + 1}/${d.getDate()}`;
}

function formatConversationType(type?: number): string {
  if (type === ConversationType.SMART_SERVICE) return "智能客服";
  if (type === ConversationType.TRIP_PLANNING) return "行程规划";
  if (type === ConversationType.ATTRACTION_CONSULT) return "景点咨询";
  return "未知类型";
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    list.value = await getConversations();
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function createNew(): Promise<void> {
  creating.value = true;
  try {
    const conversationId = await createConversation({
      title: "新的旅行咨询",
      conversationType: ConversationType.ATTRACTION_CONSULT,
    });
    await router.push(`/ai/chat/${conversationId}`);
  } catch {
    alert("创建失败");
  } finally {
    creating.value = false;
  }
}

function goDetail(id: number): void {
  router.push(`/ai/chat/${id}`);
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 900px;
  padding: 40px 24px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #000000;
}

.btn-new {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-new:hover {
  background: #00c665;
}

/* List */
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.2s;
}

.list-item:hover {
  background: #f9fff9;
  border-color: #00e676;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 0;
  font-size: 13px;
  color: #999999;
}

.dot {
  color: #dddddd;
}

.item-time {
  flex-shrink: 0;
  margin-left: 16px;
  font-size: 13px;
  color: #999999;
}

/* Empty */
.empty {
  padding: 80px 20px;
  color: #999999;
  text-align: center;
}

.empty p {
  margin: 0 0 16px 0;
  font-size: 16px;
}

.btn-link {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
}

.btn-link:hover {
  background: #00c665;
}

/* Skeleton */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-item {
  padding: 16px 20px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}

.skeleton-line {
  height: 16px;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: loading 1.5s infinite;
}

.skeleton-line.short {
  width: 40%;
  margin-bottom: 0;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
