<!-- 极简风格403页面 -->
<template>
  <div class="error-page">
    <div class="error-code">403</div>
    <h1 class="error-title">无访问权限</h1>
    <p class="error-desc">当前账号无权访问该页面</p>
    <button class="btn-home" @click="goHome">返回首页</button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
const router = useRouter();
function goHome(): void {
  router.push("/");
}
</script>

<style scoped>
.error-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
  text-align: center;
}

.error-code {
  margin-bottom: 16px;
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  color: #00e676;
}

.error-title {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
  color: #000000;
}

.error-desc {
  margin: 0 0 32px 0;
  font-size: 16px;
  color: #999999;
}

.btn-home {
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-home:hover {
  background: #00c665;
}
</style>
