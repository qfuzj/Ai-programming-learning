<!-- 浏览历史页骨架：后续展示停留时长和来源。 -->
<template>
  <PageStub title="浏览历史" description="后续接入浏览历史列表、删除单条、清空历史接口。">
    <el-table :data="[]">
      <el-table-column prop="scenicName" label="景点" />
      <el-table-column prop="visitTime" label="访问时间" />
      <el-table-column prop="stayDuration" label="停留时长" />
      <el-table-column prop="source" label="来源" />
    </el-table>
  </PageStub>
</template>

<script setup lang="ts"></script>
