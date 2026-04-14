<template>
  <div class="itinerary-list-container">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>我的行程</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建行程
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams" @submit.prevent="fetchList">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keyword" placeholder="搜行程标题或描述" clearable style="width: 200px" @clear="fetchList" @keyup.enter="fetchList" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px" @change="fetchList">
                <el-option label="草稿" :value="1" />
                <el-option label="已发布" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="是否公开">
              <el-select v-model="queryParams.isPublic" placeholder="全部分类" clearable style="width: 120px" @change="fetchList">
                <el-option label="私有" :value="0" />
                <el-option label="公开" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" native-type="submit" @click="fetchList">查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 列表 -->
          <el-table
            v-loading="loading"
            :data="list"
            border
            style="width: 100%"
          >
            <el-table-column prop="title" label="行程标题" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <el-link type="primary" @click="handleDetail(row)">{{ row.title }}</el-link>
                <el-tag v-if="row.isPublic === 1" size="small" type="success" style="margin-left: 8px">公开</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalDays" label="总天数" width="100" />
        <el-table-column prop="startDate" label="出发日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'info'">
              {{ row.status === 2 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该行程吗？此操作不可恢复"
              confirm-button-text="删除"
              confirm-button-type="danger"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑行程弹框 (也可以用独立的视图，这里选择独立组件或弹框，我们使用对话框体验更好) -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新建行程' : '编辑行程'"
      width="600px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="行程标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入行程标题 (必填，例: 重庆三日游)" />
        </el-form-item>
        <el-form-item label="出发日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择出发日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总天数" prop="totalDays">
          <el-input-number v-model="form.totalDays" :min="1" :max="90" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">草稿</el-radio>
            <el-radio :label="2">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-radio-group v-model="form.isPublic">
            <el-radio :label="0">私有</el-radio>
            <el-radio :label="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="行程简要描述..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getItineraryPage,
  createItinerary,
  updateItinerary,
  deleteItinerary,
  type ItineraryItem,
  type ItineraryQuery,
  type ItineraryCreatePayload
} from '@/api/itinerary'

const router = useRouter()

// 列表查询数据
const loading = ref(false)
const list = ref<ItineraryItem[]>([])
const total = ref(0)
const queryParams = reactive<ItineraryQuery>({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
  keyword: undefined,
  isPublic: undefined
})

// 表单数据
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const submitLoading = ref(false)
const currentEditId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()
const form = reactive<ItineraryCreatePayload>({
  title: '',
  startDate: '',
  endDate: '',
  totalDays: 1,
  status: 1,
  isPublic: 0,
  description: ''
})

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入行程标题', trigger: 'blur' }],
  totalDays: [{ required: true, message: '请填写总天数', trigger: 'blur' }]
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getItineraryPage(queryParams)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (err: any) {
    ElMessage.error(err.message || '获取行程列表失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.status = undefined
  queryParams.keyword = undefined
  queryParams.isPublic = undefined
  queryParams.pageNum = 1
  fetchList()
}

// 增删改查操作
const handleCreate = () => {
  dialogType.value = 'create'
  currentEditId.value = undefined
  // 重置表单
  Object.assign(form, {
    title: '',
    startDate: '',
    endDate: '',
    totalDays: 1,
    status: 1,
    isPublic: 0,
    description: ''
  })
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row: ItineraryItem) => {
  dialogType.value = 'edit'
  currentEditId.value = row.id
  Object.assign(form, {
    title: row.title,
    startDate: row.startDate,
    endDate: row.endDate,
    totalDays: row.totalDays || 1,
    status: row.status,
    isPublic: row.isPublic,
    description: row.description
  })
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = async (row: ItineraryItem) => {
  try {
    await deleteItinerary(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'create') {
          await createItinerary(form)
          ElMessage.success('创建成功')
        } else if (currentEditId.value) {
          await updateItinerary(currentEditId.value, form)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (err: any) {
        ElMessage.error(err.message || '保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 进入详情编排页面
const handleDetail = (row: ItineraryItem) => {
  router.push(`/itinerary/${row.id}`)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.itinerary-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.list-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.search-bar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
