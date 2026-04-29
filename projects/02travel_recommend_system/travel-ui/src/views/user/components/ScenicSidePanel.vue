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
  border: 1px solid #eaeaea;
  border-radius: 12px;
}

.ticket-card h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 800;
}

.ticket-card p {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #666;
}

.full-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  border-radius: 999px;
}

.hours-card {
  border: 1px solid #eaeaea;
  border-radius: 12px;
}

.hours-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.hours-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
}

.hours-status {
  margin-bottom: 8px;
}

.open-status {
  font-size: 15px;
  font-weight: 700;
  color: #2eac6d;
}

.hours-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 15px;
  color: #333;
}
</style>
