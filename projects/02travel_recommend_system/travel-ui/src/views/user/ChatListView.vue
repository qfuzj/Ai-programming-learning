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
            <span>
              {{
                item.conversationType === 3
                  ? "自由对话"
                  : item.conversationType === 2
                    ? "行程规划"
                    : "景点咨询"
              }}
            </span>
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
import { getConversations, createConversation, type ConversationItem } from "@/api/conversation";

const router = useRouter();
const loading = ref(false);
const creating = ref(false);
const list = ref<ConversationItem[]>([]);

function formatTime(time?: string): string {
  if (!time) return "";
  const d = new Date(time);
  return `${d.getMonth() + 1}/${d.getDate()}`;
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
    const result = await createConversation({ title: "新的旅行咨询", conversationType: 3 });
    await router.push(`/ai/chat/${result.conversationId}`);
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
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0;
}

.btn-new {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
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
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.list-item:hover {
  border-color: #00e676;
  background: #f9fff9;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 13px;
  color: #999999;
  margin: 0;
  display: flex;
  gap: 8px;
  align-items: center;
}

.dot {
  color: #dddddd;
}

.item-time {
  font-size: 13px;
  color: #999999;
  flex-shrink: 0;
  margin-left: 16px;
}

/* Empty */
.empty {
  text-align: center;
  padding: 80px 20px;
  color: #999999;
}

.empty p {
  font-size: 16px;
  margin: 0 0 16px 0;
}

.btn-link {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
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
  animation: loading 1.5s infinite;
  border-radius: 4px;
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
