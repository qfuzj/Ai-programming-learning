<template>
  <div class="user-layout">
    <header class="market-header">
      <div class="header-inner top-row">
        <div class="brand" @click="go('/')">
          <span class="brand-main">校园闲置</span>
          <span class="brand-sub">Campus</span>
        </div>

        <div class="search-wrap">
          <n-input
            v-model:value="searchKeyword"
            round
            clearable
            placeholder="搜你想要的校园二手"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon><SearchOutline /></n-icon>
            </template>
          </n-input>
          <n-button type="primary" round class="search-btn" @click="handleSearch">搜索</n-button>
        </div>

        <div class="actions">
          <template v-if="userStore.isLogin">
            <n-dropdown trigger="click" :options="userActionOptions" @select="handleUserActionSelect">
              <button class="user-menu-btn" type="button">
                <n-avatar round size="small">{{ userInitial }}</n-avatar>
                <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户中心' }}</span>
                <n-icon><ChevronDownOutline /></n-icon>
              </button>
            </n-dropdown>
          </template>
          <template v-else>
            <a @click.prevent="go('/login')"><n-icon><LogInOutline /></n-icon>登录</a>
            <a @click.prevent="go('/register')"><n-icon><PersonAddOutline /></n-icon>注册</a>
          </template>
        </div>
      </div>

      <div class="header-inner channel-row">
        <a :class="{ active: isActive('/') }" @click.prevent="go('/')">首页</a>
        <a :class="{ active: isActive('/goods') }" @click.prevent="go('/goods')">商品广场</a>
        <a :class="{ active: isActive('/publish') }" @click.prevent="go('/publish')">发布闲置</a>
        <a class="channel-hint" @click.prevent="go('/goods')">低价捡漏</a>
        <a class="channel-hint" @click.prevent="go('/goods')">数码优选</a>
        <a class="channel-hint" @click.prevent="go('/goods')">毕业季特卖</a>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <aside class="floating-tools" v-if="!isAuthPage">
      <button class="tool-item publish" @click="handleToolClick('publish')">
        <n-icon><AddCircleOutline /></n-icon>
        <span>发布</span>
      </button>
      <button class="tool-item" @click="handleToolClick('message')">
        <n-icon><ChatbubbleEllipsesOutline /></n-icon>
        <span>消息</span>
      </button>
      <button class="tool-item" @click="handleToolClick('app')">
        <n-icon><PhonePortraitOutline /></n-icon>
        <span>APP</span>
      </button>
      <button class="tool-item" @click="handleToolClick('feedback')">
        <n-icon><CreateOutline /></n-icon>
        <span>反馈</span>
      </button>
      <button class="tool-item" @click="handleToolClick('support')">
        <n-icon><HeadsetOutline /></n-icon>
        <span>客服</span>
      </button>
    </aside>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAvatar, NButton, NDropdown, NIcon, NInput, useMessage } from 'naive-ui'
import {
  AddCircleOutline,
  ChatbubbleEllipsesOutline,
  ChevronDownOutline,
  CreateOutline,
  HeadsetOutline,
  LogInOutline,
  PersonAddOutline,
  PhonePortraitOutline,
  SearchOutline,
} from '@vicons/ionicons5'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const searchKeyword = ref('')

const go = (path) => {
  if (route.path === path) {
    return
  }
  router.push(path)
}

const isActive = (path) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

const isAuthPage = computed(() => ['/login', '/register', '/admin/login'].includes(route.path))

const userInitial = computed(() => {
  const nickname = userStore.userInfo?.nickname || userStore.userInfo?.username || 'U'
  return nickname.slice(0, 1).toUpperCase()
})

const userActionOptions = computed(() => {
  const options = [
    { label: '我的订单', key: '/me/order' },
    { label: '我的发布', key: '/me/publish' },
    { label: '我的收藏', key: '/me/favorite' },
    { label: '我的消息', key: '/me/message' },
    { label: '个人资料', key: '/me/profile' },
    { label: '账号安全', key: '/me/security' }
  ]

  if (userStore.role === 'admin') {
    options.push({ type: 'divider', key: 'divider-admin' })
    options.push({ label: '进入管理后台', key: '/admin/dashboard' })
  }

  options.push({ type: 'divider', key: 'divider-logout' })
  options.push({ label: '退出登录', key: 'logout' })
  return options
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleUserActionSelect = (key) => {
  if (key === 'logout') {
    handleLogout()
    return
  }
  go(key)
}

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    message.warning('请输入搜索关键词')
    return
  }
  router.push({ path: '/goods', query: { keyword } })
}

const handleToolClick = (type) => {
  if (type === 'publish') {
    go('/publish')
    return
  }
  if (type === 'message') {
    go('/me/message')
    return
  }
  if (type === 'feedback') {
    message.info('反馈功能建设中')
    return
  }
  if (type === 'app') {
    message.info('APP 下载功能建设中')
    return
  }
  message.info('客服功能建设中')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
}

.market-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: linear-gradient(180deg, #ffe30a 0%, #ffd605 100%);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 28px;
}

.top-row {
  min-height: 72px;
  display: grid;
  grid-template-columns: auto minmax(420px, 760px) auto;
  align-items: center;
  gap: 18px;
}

.brand {
  display: flex;
  align-items: center;
  cursor: pointer;
  white-space: nowrap;
}

.brand-main {
  font-size: 40px;
  line-height: 1;
  font-weight: 900;
  color: #1f2937;
}

.brand-sub {
  margin-left: 6px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.08);
  color: #374151;
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 10px;
  width: clamp(420px, 52vw, 760px);
  justify-self: center;
}

.search-wrap :deep(.n-input-wrapper) {
  border: 2px solid #1f2937;
  border-radius: 999px;
  background: #fffef2;
  box-shadow: none;
}

.search-wrap :deep(.n-input__border),
.search-wrap :deep(.n-input__state-border) {
  display: none;
}

.search-btn {
  min-width: 100px;
  font-weight: 700;
}

.search-btn :deep(.n-button__content) {
  font-weight: 800;
}

.channel-row {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 42px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.user-menu-btn {
  border: 0;
  background: #ffffff;
  color: #111827;
  border-radius: 999px;
  padding: 4px 10px 4px 4px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.user-menu-btn:hover {
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.15);
}

a {
  color: #1f2937;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 5px 4px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 14px;
}

a:hover {
  color: #111827;
}

.channel-row a {
  position: relative;
  font-size: 15px;
}

.channel-row a.channel-hint {
  color: #475569;
  font-weight: 600;
}

.channel-row a.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -9px;
  height: 3px;
  border-radius: 3px;
  background: #111827;
}

.main {
  padding: 24px 32px 30px;
}

.floating-tools {
  position: fixed;
  right: 14px;
  top: 240px;
  width: 58px;
  border-radius: 28px;
  background: #fff;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.15);
  border: 1px solid #e5e7eb;
  display: grid;
  gap: 4px;
  padding: 8px 4px;
  z-index: 30;
}

.tool-item {
  border: 0;
  background: transparent;
  color: #334155;
  cursor: pointer;
  border-radius: 12px;
  padding: 8px 0;
  display: grid;
  justify-items: center;
  gap: 2px;
  font-size: 12px;
}

.tool-item.publish {
  color: #111827;
  background: #ffe30a;
}

.tool-item:hover {
  background: #f8fafc;
}

@media (max-width: 980px) {
  .top-row {
    grid-template-columns: 1fr;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .search-wrap {
    width: min(92vw, 680px);
  }

  .brand {
    justify-content: center;
  }

  .actions {
    justify-content: center;
  }

  .channel-row {
    justify-content: center;
    padding-bottom: 8px;
  }

  .main {
    padding: 18px 12px 22px;
  }

  .floating-tools {
    display: none;
  }
}
</style>
