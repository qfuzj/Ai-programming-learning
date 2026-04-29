<!-- 极简风格用户端布局 -->
<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="brand">智游</router-link>

        <nav class="nav">
          <router-link to="/" class="nav-link" :class="{ active: isActive('/') }">发现</router-link>
          <router-link to="/scenic" class="nav-link" :class="{ active: isActive('/scenic') }">
            景点
          </router-link>
          <router-link
            to="/ai/recommend"
            class="nav-link"
            :class="{ active: isActive('/ai/recommend') }"
          >
            推荐
          </router-link>
          <router-link to="/ai/chat" class="nav-link" :class="{ active: isActive('/ai/chat') }">
            对话
          </router-link>
        </nav>

        <div class="header-right">
          <template v-if="userStore.isAuthenticated">
            <div class="user-menu" @click="showDropdown = !showDropdown">
              <div class="avatar">{{ displayUserName.charAt(0) }}</div>
              <span class="username">{{ displayUserName }}</span>
            </div>
            <div v-if="showDropdown" class="dropdown" @click.stop>
              <router-link to="/profile" class="dropdown-item">个人中心</router-link>
              <router-link to="/itinerary" class="dropdown-item">我的行程</router-link>
              <router-link to="/itinerary/ai-generate" class="dropdown-item">
                AI生成行程
              </router-link>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item danger" @click="onLogout">退出登录</div>
            </div>
          </template>
          <button v-else class="btn-login" @click="router.push(ROUTE_PATHS.USER_LOGIN)">
            登录
          </button>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const showDropdown = ref(false);

const displayUserName = computed(() => {
  return userStore.profile?.nickname || userStore.profile?.username || "用户";
});

function isActive(path: string): boolean {
  if (path === "/") return route.path === "/";
  return route.path.startsWith(path);
}

async function onLogout(): Promise<void> {
  try {
    await userStore.logout();
  } finally {
    showDropdown.value = false;
    await router.push(ROUTE_PATHS.USER_LOGIN);
  }
}

function handleClickOutside(e: Event) {
  const target = e.target as HTMLElement;
  if (!target.closest(".user-menu") && !target.closest(".dropdown")) {
    showDropdown.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #ffffff;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: 1200px;
  height: 64px;
  padding: 0 24px;
  margin: 0 auto;
}

.brand {
  font-size: 24px;
  font-weight: 800;
  color: #000000;
  letter-spacing: -0.5px;
}

.nav {
  display: flex;
  gap: 32px;
  align-items: center;
  margin-left: 64px;
}

.nav-link {
  position: relative;
  padding: 4px 0;
  font-size: 15px;
  font-weight: 500;
  color: #999999;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #000000;
}

.nav-link.active {
  color: #000000;
}

.nav-link.active::after {
  position: absolute;
  right: 0;
  bottom: -20px;
  left: 0;
  height: 2px;
  content: "";
  background: #00e676;
}

.header-right {
  position: relative;
  margin-left: auto;
}

.user-menu {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 999px;
  transition: background 0.2s;
}

.user-menu:hover {
  background: #f5f5f5;
}

.avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  font-size: 14px;
  font-weight: 700;
  color: #000000;
  background: #00e676;
  border-radius: 50%;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  white-space: nowrap;
}

.dropdown {
  position: absolute;
  top: 52px;
  right: 0;
  min-width: 180px;
  padding: 8px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  font-size: 14px;
  color: #000000;
  border-radius: 8px;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.dropdown-item.danger {
  color: #ff5252;
}

.dropdown-divider {
  height: 1px;
  margin: 4px 0;
  background: #f0f0f0;
}

.btn-login {
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 999px;
  transition: background 0.2s;
}

.btn-login:hover {
  background: #00c665;
}

.main {
  min-height: calc(100vh - 64px);
}
</style>
