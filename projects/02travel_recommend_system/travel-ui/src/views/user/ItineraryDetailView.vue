<!-- 行程详情页骨架：后续联调按天编辑和行程项管理。 -->
<template>
  <PageStub :title="`行程详情 #${planId}`" description="后续展示每天的行程项，并支持增删改。">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="计划ID">{{ planId }}</el-descriptions-item>
      <el-descriptions-item label="说明">这里是行程详情骨架</el-descriptions-item>
    </el-descriptions>
  </PageStub>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const planId = computed(() => route.params.id ?? "-");
</script>
