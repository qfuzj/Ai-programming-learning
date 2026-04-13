<!-- 地区管理页骨架：后续联调树形地区维护。 -->
<template>
  <PageStub title="地区管理" description="后续接入地区树和增删改操作。">
    <el-tree :data="[]" default-expand-all node-key="id" />
  </PageStub>
</template>

<script setup lang="ts"></script>

<style scoped>
.page-container {
  padding: 0px;
}
</style>
