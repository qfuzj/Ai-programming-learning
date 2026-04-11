<!-- 行程列表骨架：后续联调行程计划 CRUD。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>行程列表</template>
      <el-button type="primary">新建行程</el-button>
      <el-table :data="[]" style="margin-top: 12px">
        <el-table-column prop="title" label="行程标题" />
        <el-table-column prop="startDate" label="开始日期" />
        <el-table-column prop="endDate" label="结束日期" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup lang="ts"></script>
