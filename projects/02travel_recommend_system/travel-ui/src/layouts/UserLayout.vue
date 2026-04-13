<!-- 用户端布局：顶部导航 + 主内容区域。 -->
<template>
  <el-container class="user-layout" direction="vertical">
    <el-header class="user-header">
      <div class="header-inner">
        <router-link to="/" class="brand">智游 Advisor</router-link>
        <div class="header-center">
          <el-menu
            :default-active="activePath"
            mode="horizontal"
            router
            :ellipsis="false"
            class="center-menu"
          >
            <el-menu-item index="/">发现</el-menu-item>
            <el-menu-item index="/scenic">景点</el-menu-item>
            <el-menu-item index="/my-reviews">点评</el-menu-item>
          </el-menu>
        </div>
        <div class="header-right">
          <el-dropdown v-if="userStore.isAuthenticated" trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar
                :size="32"
                :src="
                  userStore.profile?.avatar ||
                  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                "
              />
              <span class="user-name">
                {{ userStore.profile?.nickname || userStore.profile?.username || "个人中心" }}
              </span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="/favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="/history">浏览历史</el-dropdown-item>
                <el-dropdown-item command="/itinerary">我的行程</el-dropdown-item>
                <el-dropdown-item command="/ai/recommend">AI 推荐</el-dropdown-item>
                <el-dropdown-item command="/ai/chat">AI 聊天</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            v-else
            type="primary"
            round
            class="login-btn"
            @click="router.push(ROUTE_PATHS.USER_LOGIN)"
          >
            登录
          </el-button>
        </div>
      </div>
    </el-header>
    <el-main class="user-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";
import { ArrowDown } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const USER_MENU_INDEXES = [ROUTE_PATHS.USER_HOME, "/scenic", "/my-reviews"] as const;

const activePath = computed(() => {
  const currentPath = route.path;

  if (currentPath === ROUTE_PATHS.USER_HOME) {
    return ROUTE_PATHS.USER_HOME;
  }

  const matchedMenu = USER_MENU_INDEXES.find(
    (menuPath) =>
      menuPath !== ROUTE_PATHS.USER_HOME &&
      (currentPath === menuPath || currentPath.startsWith(`${menuPath}/`))
  );

  return matchedMenu ?? currentPath;
});

async function handleCommand(command: string) {
  if (command === "logout") {
    await onLogout();
  } else {
    void router.push(command);
  }
}

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
  width: 100% !important;
  max-width: 100% !important;
  min-height: 100vh;
}

:deep(.el-container) {
  width: 100% !important;
}

.user-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 54px;
  padding: 0;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  height: 100%;
  padding: 0 24px;
  margin: 0 auto;
}

.brand {
  flex-shrink: 0;
  font-size: 24px;
  font-weight: 700;
  color: #000;
  text-decoration: none;
  cursor: pointer;
}

.header-center {
  display: flex;
  flex: 1;
  justify-content: center;
}

.center-menu {
  height: 54px;
  font-size: 16px;
  font-weight: 500;
  background-color: transparent;
  border-bottom: none !important;
}

.center-menu:deep(.el-menu-item),
.center-menu:deep(.el-sub-menu__title) {
  font-size: 16px;
  color: #000;
  background-color: transparent !important;
  border-bottom: 2px solid transparent !important;
}

.center-menu:deep(.el-menu-item:hover),
.center-menu:deep(.el-sub-menu__title:hover) {
  color: #000;
  border-bottom-color: #000 !important;
}

.center-menu:deep(.el-menu-item.is-active) {
  color: #000 !important;
  border-bottom: 2px solid #000 !important;
}

.header-right {
  display: flex;
  flex-shrink: 0;
  gap: 12px;
  align-items: center;
}

.user-dropdown {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 4px 12px 4px 6px;
  font-size: 15px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  border-radius: 999px;
  transition: background-color 0.2s;
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-main {
  position: relative;
  top: 0;
  flex: 1;
  width: 100% !important;
  min-width: 0;
  padding: 0;
  overflow: visible;
  /* Prevent el-main from clipping vertically */
  background: #fff;
}

.login-btn {
  color: #fff;
  background-color: #000;
  border: none;
}

.login-btn:hover {
  background-color: #333;
}
</style>
