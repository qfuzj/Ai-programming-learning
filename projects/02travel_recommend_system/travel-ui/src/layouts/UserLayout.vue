<!-- 用户端布局：顶部导航 + 主内容区域。 -->
<template>
  <div class="user-layout">
    <el-header class="user-header">
      <div class="brand">LLM-Travel-Advisor</div>
      <el-menu :default-active="activePath" mode="horizontal" router>
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/scenic">景点</el-menu-item>
        <el-menu-item index="/favorites">收藏</el-menu-item>
        <el-menu-item index="/history">历史</el-menu-item>
        <el-menu-item index="/my-reviews">点评</el-menu-item>
        <el-menu-item index="/ai/recommend">AI 推荐</el-menu-item>
        <el-menu-item index="/ai/chat">AI 聊天</el-menu-item>
        <el-menu-item index="/itinerary">行程</el-menu-item>
        <el-menu-item index="/profile">我的</el-menu-item>
      </el-menu>
      <el-button v-if="userStore.isAuthenticated" type="primary" text @click="onLogout">退出登录</el-button>
      <el-button v-else type="primary" text @click="router.push(ROUTE_PATHS.USER_LOGIN)">登录</el-button>
    </el-header>
    <el-main class="user-main">
      <router-view />
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const USER_MENU_INDEXES = [
  ROUTE_PATHS.USER_HOME,
  "/scenic",
  "/favorites",
  "/history",
  "/my-reviews",
  "/ai/recommend",
  "/ai/chat",
  "/itinerary",
  "/profile",
] as const;

const activePath = computed(() => {
  const currentPath = route.path;
  if (currentPath === ROUTE_PATHS.USER_HOME) {
    return ROUTE_PATHS.USER_HOME;
  }

  const matchedMenu = USER_MENU_INDEXES.find(
    (menuPath) =>
      menuPath !== ROUTE_PATHS.USER_HOME && (currentPath === menuPath || currentPath.startsWith(`${menuPath}/`)),
  );

  return matchedMenu ?? currentPath;
});

async function onLogout(): Promise<void> {
  try {
    await userStore.logout();
  } finally {
    await router.push(ROUTE_PATHS.USER_LOGIN);
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100%;
}

.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.brand {
  font-size: 18px;
  font-weight: 600;
  margin-right: 24px;
}

.user-main {
  min-height: calc(100vh - 60px);
}
</style>
