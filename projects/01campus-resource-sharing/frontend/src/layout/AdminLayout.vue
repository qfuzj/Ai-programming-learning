<template>
  <div class="admin-layout">
    <aside class="sider">
      <div class="brand">
        <h2>Campus Admin</h2>
        <p>校园资源后台</p>
      </div>

      <nav class="menu">
        <div
          v-for="item in menuList"
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          @click="go(item.path)"
        >
          <n-icon><component :is="item.icon" /></n-icon>
          {{ item.label }}
        </div>
      </nav>
    </aside>

    <section class="content">
      <header class="header">
        <div class="header-left">管理后台</div>
        <div class="header-right">
          <span class="admin-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
          <button class="link-btn" @click="goFront"><n-icon><HomeOutline /></n-icon>前台首页</button>
          <button class="danger-btn" @click="handleLogout"><n-icon><LogOutOutline /></n-icon>退出登录</button>
        </div>
      </header>

      <main class="main">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { NIcon, useMessage } from 'naive-ui'
import {
  HomeOutline,
  LogOutOutline,
  MegaphoneOutline,
  PieChartOutline,
  PricetagOutline,
  ReceiptOutline,
  RibbonOutline,
  ShieldOutline,
  StorefrontOutline,
  TextOutline,
  PeopleOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const menuList = [
  { path: '/admin/dashboard', label: '控制台', icon: PieChartOutline },
  { path: '/admin/user', label: '用户管理', icon: PeopleOutline },
  { path: '/admin/goods', label: '商品管理', icon: StorefrontOutline },
  { path: '/admin/category', label: '分类管理', icon: PricetagOutline },
  { path: '/admin/order', label: '订单管理', icon: ReceiptOutline },
  { path: '/admin/notice', label: '公告管理', icon: MegaphoneOutline },
  { path: '/admin/banner', label: '轮播管理', icon: RibbonOutline },
  { path: '/admin/report', label: '举报管理', icon: ShieldOutline },
  { path: '/admin/comment', label: '评论管理', icon: TextOutline }
]

const isActive = (path) => route.path === path

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}

const goFront = () => {
  router.push('/')
}

const handleLogout = () => {
  userStore.logout()
  message.success('已退出登录')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
}

.sider {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
  padding: 20px 14px;
}

.brand {
  padding: 10px 10px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.brand h2 {
  margin: 0;
  font-size: 20px;
}

.brand p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #cbd5e1;
}

.menu {
  padding: 16px 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #e2e8f0;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.12);
}

.menu-item.active {
  background: #2563eb;
  color: #fff;
}

.header {
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.header-left {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-name {
  color: #64748b;
  font-size: 14px;
}

.link-btn,
.danger-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  cursor: pointer;
}

.danger-btn {
  border-color: #fecaca;
  color: #b91c1c;
}

.main {
  padding: 20px;
  background: #f8fafc;
}

@media (max-width: 900px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .sider {
    display: none;
  }
}
</style>
