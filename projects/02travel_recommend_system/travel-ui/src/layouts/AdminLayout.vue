<!-- 极简风格管理端布局 -->
<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">管理后台</div>
      <nav class="nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          {{ item.label }}
        </router-link>

        <div class="nav-group">
          <div class="nav-group-title">数据分析</div>
          <router-link
            to="/admin/analytics/recommend"
            class="nav-item"
            :class="{ active: isActive('/admin/analytics/recommend') }"
          >
            推荐分析
          </router-link>
          <router-link
            to="/admin/analytics/llm"
            class="nav-item"
            :class="{ active: isActive('/admin/analytics/llm') }"
          >
            LLM 分析
          </router-link>
        </div>
      </nav>
    </aside>

    <div class="content-area">
      <header class="header">
        <span class="header-title">{{ currentPageTitle }}</span>
        <button class="btn-logout" @click="onLogout">退出登录</button>
      </header>
      <main class="main">
        <router-view />
      </main>
    </div>
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

const menuItems = [
  { path: "/admin/dashboard", label: "工作台" },
  { path: "/admin/scenic", label: "景点管理" },
  { path: "/admin/audit", label: "审核管理" },
  { path: "/admin/user", label: "用户管理" },
  { path: "/admin/regions", label: "地区管理" },
  { path: "/admin/tags", label: "标签管理" },
  { path: "/admin/system-config", label: "系统配置" },
  { path: "/admin/logs", label: "操作日志" },
];

const currentPageTitle = computed(() => {
  const matched = menuItems.find((item) => route.path.startsWith(item.path));
  if (matched) return matched.label;
  if (route.path.includes("recommend")) return "推荐分析";
  if (route.path.includes("llm")) return "LLM 分析";
  return "管理后台";
});

function isActive(path: string): boolean {
  if (path === "/admin/dashboard") return route.path === path;
  return route.path.startsWith(path);
}

async function onLogout(): Promise<void> {
  try {
    await userStore.logout();
  } finally {
    await router.push(ROUTE_PATHS.ADMIN_LOGIN);
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.sidebar {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  width: 240px;
  overflow-y: auto;
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
}

.brand {
  padding: 20px 24px;
  font-size: 18px;
  font-weight: 800;
  color: #000000;
  border-bottom: 1px solid #f0f0f0;
}

.nav {
  flex: 1;
  padding: 16px 12px;
}

.nav-item {
  display: block;
  padding: 10px 16px;
  margin-bottom: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #999999;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  color: #000000;
  background: #f5f5f5;
}

.nav-item.active {
  font-weight: 600;
  color: #000000;
  background: #e8f5e9;
}

.nav-group {
  margin-top: 24px;
}

.nav-group-title {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #999999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.content-area {
  display: flex;
  flex: 1;
  flex-direction: column;
  margin-left: 240px;
}

.header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 32px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
}

.btn-logout {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #ff5252;
  cursor: pointer;
  background: transparent;
  border: 1px solid #ff5252;
  border-radius: 999px;
  transition: all 0.2s;
}

.btn-logout:hover {
  color: #ffffff;
  background: #ff5252;
}

.main {
  flex: 1;
  padding: 32px;
  background: #f5f5f5;
}
</style>
