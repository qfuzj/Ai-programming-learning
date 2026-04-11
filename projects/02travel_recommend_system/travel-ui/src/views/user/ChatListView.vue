<!-- AI 会话列表页：展示会话并支持新建。 -->
<template>
  <PageStub title="AI 对话" description="进入会话详情后可继续追问。">
    <template #actions>
      <el-button type="primary" :loading="creating" @click="createNewConversation">
        新建会话
      </el-button>
    </template>

    <el-table v-loading="loading" :data="conversations" @row-click="goDetail">
      <el-table-column prop="title" label="会话标题" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="primary" text @click.stop="goDetail(scope.row)">进入</el-button>
        </template>
      </el-table-column>
    </el-table>
  </PageStub>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { createConversation, getConversations, type ConversationItem } from "@/api/chat";

const router = useRouter();
const loading = ref(false);
const creating = ref(false);
const conversations = ref<ConversationItem[]>([]);

async function loadConversations(): Promise<void> {
  loading.value = true;
  try {
    conversations.value = await getConversations();
  } finally {
    loading.value = false;
  }
}

async function createNewConversation(): Promise<void> {
  creating.value = true;
  try {
    const result = await createConversation({ title: "新的旅行咨询", scene: "travel_consult" });
    await router.push(`/ai/chat/${result.conversationId}`);
  } finally {
    creating.value = false;
  }
}

function goDetail(row: ConversationItem): void {
  void router.push(`/ai/chat/${row.conversationId}`);
}

onMounted(() => {
  void loadConversations();
});
</script>
