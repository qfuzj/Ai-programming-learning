<template>
  <div>
    <el-table v-loading="loading" :data="list" border style="width: 100%">
      <el-table-column prop="title" label="行程标题" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="emit('detail', row)">{{ row.title }}</el-link>
          <el-tag v-if="row.isPublic === 1" size="small" type="success" style="margin-left: 8px">公开</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="totalDays" label="总天数" width="100" />
      <el-table-column prop="startDate" label="出发日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />

      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : 'info'">
            {{ row.status === 2 ? "已发布" : "草稿" }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createdAt" label="创建时间" width="180" />

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="emit('detail', row)">详情</el-button>
          <el-button link type="primary" @click="emit('edit', row)">编辑</el-button>
          <el-popconfirm
            title="确定删除该行程吗？此操作不可恢复"
            confirm-button-text="删除"
            confirm-button-type="danger"
            @confirm="emit('delete', row)"
          >
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="(size) => emit('size-change', size)"
        @current-change="(page) => emit('page-change', page)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ItineraryItem } from "@/api/itinerary";

interface Props {
  loading: boolean;
  list: ItineraryItem[];
  total: number;
  currentPage: number;
  pageSize: number;
}

defineProps<Props>();

const emit = defineEmits<{
  detail: [row: ItineraryItem];
  edit: [row: ItineraryItem];
  delete: [row: ItineraryItem];
  "page-change": [page: number];
  "size-change": [size: number];
}>();
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
