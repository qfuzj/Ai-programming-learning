<template>
  <div class="sticky-block">
    <el-card class="ticket-card" shadow="never">
      <h3>游览和体验</h3>
      <p>探索体验此地点的不同方式。</p>
      <el-button type="primary" class="full-btn">
        查看选项 / 门票 ¥{{ ticketPrice ?? "咨询" }}
      </el-button>
    </el-card>

    <el-card class="hours-card" shadow="never">
      <div class="hours-header">
        <h3>营业时间</h3>
        <el-icon><ArrowDown /></el-icon>
      </div>
      <div class="hours-status">
        <span class="open-status">正在营业</span>
      </div>
      <div class="hours-detail">
        <span>今日</span>
        <span>{{ openTime || "全天开放" }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown } from "@element-plus/icons-vue";

interface Props {
  ticketPrice?: number;
  openTime?: string;
}

defineProps<Props>();
</script>

<style scoped>
.sticky-block {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ticket-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}

.ticket-card h3 {
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 8px 0;
}

.ticket-card p {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

.full-btn {
  width: 100%;
  height: 48px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}

.hours-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}

.hours-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.hours-header h3 {
  font-size: 18px;
  font-weight: 800;
  margin: 0;
}

.hours-status {
  margin-bottom: 8px;
}

.open-status {
  color: #2eac6d;
  font-weight: 700;
  font-size: 15px;
}

.hours-detail {
  display: flex;
  justify-content: space-between;
  font-size: 15px;
  color: #333;
  margin-top: 8px;
}
</style>
