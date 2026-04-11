<!-- 403 页面：角色不匹配时的统一兜底页。 -->
<template>
  <div class="page-container">
    <el-result icon="warning" title="403" sub-title="当前账号无权访问该页面">
      <template #extra>
        <el-button type="primary" @click="goBack">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();
function goBack(): void {
  void router.push("/");
}
</script>
