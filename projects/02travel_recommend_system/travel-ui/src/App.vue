<!-- 应用根组件：仅承载路由视图。 -->
<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useUserStore } from '@/store';

const userStore = useUserStore();

onMounted(async () => {
  // 如果当前存在 token 但内存中没 profile，或者即便有持久化的 profile 但为了保证最新，触发再拉取一次
  if (userStore.isAuthenticated) {
    try {
      await userStore.fetchProfile();
    } catch (e) {
      console.warn("Failed to fetch fresh profile on load:", e);
    }
  }
});
</script>
