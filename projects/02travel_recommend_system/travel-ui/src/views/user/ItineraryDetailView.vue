<template>
  <div class="itinerary-detail-container" v-loading="loading">
    <div class="header-section">
      <el-page-header @back="router.back()">
        <template #content>
          <div class="title-with-tags">
            <span class="text-large font-bold mr-3">{{ detail?.title || '行程详情' }}</span>
            <el-tag v-if="detail?.status === 2" type="success" size="small">已发布</el-tag>
            <el-tag v-else type="info" size="small">草稿</el-tag>
            <el-tag v-if="detail?.isPublic === 1" type="success" size="small">公开</el-tag>
          </div>
        </template>
      </el-page-header>

      <el-descriptions :column="3" border style="margin-top: 20px">
        <el-descriptions-item label="出发日期">{{ detail?.startDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detail?.endDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总天数">{{ detail?.totalDays || 1 }} 天</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail?.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail?.description || '暂无描述' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <el-card class="days-card" shadow="never">
      <el-tabs v-model="activeDay" class="days-tabs">
        <!-- Render a tab for each day based on totalDays -->
        <el-tab-pane
          v-for="day in (detail?.totalDays || 1)"
          :key="day"
          :label="`Day ${day}`"
          :name="String(day)"
        >
          <div class="day-content">
            <div class="day-header">
              <h3>第 {{ day }} 天行程</h3>
              <el-button type="primary" size="small" @click="handleAddItem(day)">
                <el-icon><Plus /></el-icon>添加项目
              </el-button>
            </div>
            
            <el-timeline v-if="getDayItems(day).length > 0" style="margin-top: 20px">
              <el-timeline-item
                v-for="(item, index) in getDayItems(day)"
                :key="item.id"
                :timestamp="(item.startTime || '') + (item.endTime ? ' ~ ' + item.endTime : '')"
                placement="top"
              >
                <el-card class="timeline-card" shadow="hover">
                  <div class="item-header">
                    <h4>{{ item.title }}</h4>
                    <div class="item-actions">
                      <el-button link type="danger" size="small" @click="handleDeleteItem(item.id)">删除</el-button>
                    </div>
                  </div>
                  <p v-if="item.description" class="item-desc">{{ item.description }}</p>
                  <p v-if="item.scenicSpotId" class="item-spot">关联景点 ID: {{ item.scenicSpotId }}</p>
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

    <!-- Create Item Dialog -->
    <el-dialog v-model="itemDialogVisible" title="添加行程项" width="500px" append-to-body>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="itemForm.title" placeholder="如: 锦里古街游玩" />
        </el-form-item>
        <el-form-item label="活动类型" prop="itemType">
          <el-select v-model="itemForm.itemType" placeholder="选择类型" style="width: 100%">
            <el-option label="景点" :value="1" />
            <el-option label="餐饮" :value="2" />
            <el-option label="住宿" :value="3" />
            <el-option label="交通" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="itemForm.startTime"
            start="06:00"
            step="00:30"
            end="23:30"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="itemForm.endTime"
            start="06:00"
            step="00:30"
            end="23:30"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="itemForm.description" type="textarea" :rows="2" placeholder="活动描述" />
        </el-form-item>
        <el-form-item label="预算(元)">
          <el-input-number v-model="itemForm.estimatedCost" :min="0" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitItem" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getItineraryDetail, addItineraryItem, deleteItineraryItem } from '@/api/itinerary'
import type { ItineraryItem, ItineraryItemPayload } from '@/api/itinerary'

const route = useRoute()
const router = useRouter()
const planId = Number(route.params.id)

const loading = ref(false)
const detail = ref<ItineraryItem | null>(null)
const activeDay = ref('1')

const fetchDetail = async () => {
  if (!planId) return
  loading.value = true
  try {
    const res = await getItineraryDetail(planId)
    detail.value = res
  } catch (err: any) {
    ElMessage.error(err.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

// 辅助方法：获取某一天的所有 items
const getDayItems = (day: number) => {
  if (!detail.value || !detail.value.days) return []
  const targetDay = detail.value.days.find(d => d.dayNo === day)
  return targetDay ? targetDay.items : []
}

// 行程项弹框
const itemDialogVisible = ref(false)
const submitLoading = ref(false)
const itemFormRef = ref<FormInstance>()
const currentAddDay = ref(1)

const itemForm = ref<ItineraryItemPayload>({
  dayNo: 1,
  title: '',
  itemType: 1,
  startTime: '',
  endTime: '',
  description: '',
  estimatedCost: 0
})

const itemRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  itemType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const handleAddItem = (day: number) => {
  currentAddDay.value = day
  itemForm.value = {
    dayNo: day,
    title: '',
    itemType: 1,
    startTime: '',
    endTime: '',
    description: '',
    estimatedCost: 0
  }
  if (itemFormRef.value) itemFormRef.value.clearValidate()
  itemDialogVisible.value = true
}

const submitItem = async () => {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await addItineraryItem(planId, itemForm.value)
        ElMessage.success('添加成功')
        itemDialogVisible.value = false
        // 重新获取详情以刷新列表
        fetchDetail()
      } catch (err: any) {
        ElMessage.error(err.message || '添加失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDeleteItem = async (itemId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该行程项吗？此操作不可恢复', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteItineraryItem(planId, itemId)
    ElMessage.success('已删除')
    fetchDetail()
  } catch (cancelOrError: any) {
    if (cancelOrError !== 'cancel') {
      ElMessage.error(cancelOrError.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.itinerary-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.header-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.title-with-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}
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
.item-spot, .item-loc, .item-notes {
  color: #909399;
  font-size: 13px;
  margin: 2px 0;
}
</style>
