<!-- 我的点评页骨架：后续联调点评列表与删除操作。 -->
<template>
  <PageStub title="我的点评" description="后续接入点评列表与审核状态展示。">
    <el-table :data="[]">
      <el-table-column prop="scenicName" label="景点" />
      <el-table-column prop="score" label="评分" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </PageStub>
</template>

<script setup lang="ts"></script>