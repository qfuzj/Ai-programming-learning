<!-- 404 页面：路由未命中时显示。 -->
<template>
  <div class="page-container">
    <el-result icon="info" title="404" sub-title="页面不存在或已被移除">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();
function goHome(): void {
  void router.push("/");
}
</script>
