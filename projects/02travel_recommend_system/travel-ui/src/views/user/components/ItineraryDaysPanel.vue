<template>
  <el-card class="days-card" shadow="never">
    <el-tabs
      :model-value="activeDay"
      class="days-tabs"
      @update:model-value="(value) => emit('update:activeDay', String(value))"
    >
      <el-tab-pane
        v-for="day in detail?.totalDays || 1"
        :key="day"
        :label="`Day ${day}`"
        :name="String(day)"
      >
        <div class="day-content">
          <div class="day-header">
            <h3>第 {{ day }} 天行程</h3>
            <el-button type="primary" size="small" @click="emit('add-item', day)">
              添加项目
            </el-button>
          </div>

          <el-timeline v-if="getDayItems(day).length > 0" style="margin-top: 20px">
            <el-timeline-item
              v-for="item in getDayItems(day)"
              :key="item.id"
              :timestamp="(item.startTime || '') + (item.endTime ? ' ~ ' + item.endTime : '')"
              placement="top"
            >
              <el-card class="timeline-card" shadow="hover">
                <div class="item-header">
                  <h4>{{ item.title }}</h4>
                  <div class="item-actions">
                    <el-button
                      link
                      type="danger"
                      size="small"
                      @click="emit('delete-item', item.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <p v-if="item.description" class="item-desc">{{ item.description }}</p>
                <p v-if="item.scenicSpotId" class="item-spot">
                  关联景点 ID: {{ item.scenicSpotId }}
                </p>
                <p v-if="item.location" class="item-loc">位置: {{ item.location }}</p>
                <p v-if="item.estimatedCost">预计花费: ¥{{ item.estimatedCost }}</p>
                <p v-if="item.notes" class="item-notes">备注: {{ item.notes }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-else description="这天还没有安排行程" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import type { ItineraryDayItem, ItineraryItem } from "@/api/itinerary";

interface Props {
  detail: ItineraryItem | null;
  activeDay: string;
  getDayItems: (day: number) => ItineraryDayItem["items"];
}

defineProps<Props>();

const emit = defineEmits<{
  "update:activeDay": [value: string];
  "add-item": [day: number];
  "delete-item": [itemId: number];
}>();
</script>

<style scoped>
.days-card {
  border-radius: 8px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-card {
  margin-bottom: 8px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-header h4 {
  margin: 0;
  font-size: 16px;
}

.item-desc {
  color: #606266;
  font-size: 14px;
  margin: 4px 0;
}

.item-spot,
.item-loc,
.item-notes {
  color: #909399;
  font-size: 13px;
  margin: 2px 0;
}
</style>
