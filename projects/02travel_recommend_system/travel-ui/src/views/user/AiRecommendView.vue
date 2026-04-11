<!-- AI 推荐页骨架：后续联调推荐接口与反馈行为。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>AI 推荐</template>
      <el-alert type="info" :closable="false" title="后续接入 /api/user/recommend/home 与反馈接口" />
      <el-table :data="[]" style="margin-top: 12px">
        <el-table-column prop="scenicName" label="景点" />
        <el-table-column prop="reason" label="推荐理由" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup lang="ts"></script>
