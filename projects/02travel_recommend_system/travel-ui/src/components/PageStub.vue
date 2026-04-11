<!-- 通用页面骨架：用于快速承载标题、说明和后续扩展位。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="header-row">
          <span>{{ title }}</span>
          <slot name="actions" />
        </div>
      </template>

      <el-alert v-if="description" :title="description" type="info" :closable="false" show-icon />
      <div class="content-slot">
        <slot />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
}>();
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.content-slot {
  margin-top: 16px;
}
</style>