<template>
  <section class="info-section">
    <h2>相关信息</h2>
    <p class="scenic-intro" :class="{ expanded: introExpanded }">
      {{ detail.description || "暂无详细简介。请查看体验和门票信息了解详情。" }}
      <template v-if="detail.detailContent">
        <br />
        <br />
        {{ detail.detailContent }}
      </template>
    </p>
    <div
      v-if="detail.description && detail.description.length > 100"
      class="read-more-link"
      @click="emit('toggle-intro')"
    >
      {{ introExpanded ? "收起" : "阅读更多" }}
      <el-icon>
        <ArrowDown v-if="!introExpanded" />
        <ArrowUp v-else />
      </el-icon>
    </div>

    <div style="margin-top: 16px; font-size: 15px; color: #333">
      <p>
        <strong>地理位置：</strong>
        {{ detail.address || "暂无位置信息" }}
      </p>
      <p v-if="detail.level">
        <strong>景区等级：</strong>
        {{ detail.level }} 景区
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ScenicDetailModel } from "@/types/scenic-detail";
import { ArrowDown, ArrowUp } from "@element-plus/icons-vue";

interface Props {
  detail: ScenicDetailModel;
  introExpanded: boolean;
}

defineProps<Props>();
const emit = defineEmits<{
  "toggle-intro": [];
}>();
</script>

<style scoped>
.info-section {
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.info-section h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
}

.scenic-intro {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 4;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  -webkit-box-orient: vertical;
}

.scenic-intro.expanded {
  display: block;
  -webkit-line-clamp: unset;
}

.read-more-link {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  margin-top: 10px;
  font-weight: 700;
  color: #1a1a1a;
  text-decoration: underline;
  text-underline-offset: 2px;
  cursor: pointer;
}
</style>
