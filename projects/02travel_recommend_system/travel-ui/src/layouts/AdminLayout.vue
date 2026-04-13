<!-- 管理端布局：侧边栏 + 顶栏 + 内容区域。 -->
<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-brand">管理后台</div>
      <el-menu :default-active="activePath" router>
        <el-menu-item index="/admin/dashboard">工作台</el-menu-item>
        <el-menu-item index="/admin/scenic">景点管理</el-menu-item>
        <el-menu-item index="/admin/review">评论审核</el-menu-item>
        <el-menu-item index="/admin/user">用户管理</el-menu-item>
        <el-menu-item index="/admin/regions">地区管理</el-menu-item>
        <el-menu-item index="/admin/tags">标签管理</el-menu-item>
        <el-menu-item index="/admin/system-config">系统配置</el-menu-item>
        <el-menu-item index="/admin/logs">操作日志</el-menu-item>
        <el-sub-menu index="/admin/analytics">
          <template #title>数据分析</template>
          <el-menu-item index="/admin/analytics/recommend">推荐分析</el-menu-item>
          <el-menu-item index="/admin/analytics/llm">LLM 分析</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <span>LLM-Travel-Advisor 管理端</span>
        <el-button type="danger" text @click="onLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const activePath = computed(() => route.path);

async function onLogout(): Promise<void> {
  try {
    await userStore.logout();
  } finally {
    await router.push(ROUTE_PATHS.ADMIN_LOGIN);
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
}

.admin-brand {
  padding: 18px 16px;
  font-weight: 700;
  border-bottom: 1px solid #ebeef5;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.el-main {
  padding: 4px;
}
</style>
