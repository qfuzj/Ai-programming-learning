<template>
  <div class="admin-page">
    <n-card title="举报管理" :bordered="false">
      <div class="toolbar">
        <n-input-number v-model:value="query.reporterId" :min="1" placeholder="举报人ID" clearable />
        <n-input v-model:value="query.reporterKeyword" placeholder="举报人姓名关键词" clearable />
        <n-select v-model:value="query.targetType" :options="targetTypeOptions" clearable placeholder="举报对象" />
        <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="审核状态" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
      </div>

      <n-data-table :columns="columns" :data="page.records" :loading="loading" :bordered="false" />

      <div class="pager">
        <n-pagination
          v-model:page="query.pageNum"
          v-model:page-size="query.pageSize"
          :item-count="page.total"
          show-size-picker
          :page-sizes="[10, 20, 50]"
          @update:page="loadList"
          @update:page-size="loadList"
        />
      </div>

      <n-modal v-model:show="reviewModal.show" preset="card" title="举报审核" style="width: 560px">
        <n-form :model="reviewModal.form" label-placement="top">
          <n-form-item label="审核结果">
            <n-select v-model:value="reviewModal.form.status" :options="reviewStatusOptions" />
          </n-form-item>
          <n-form-item label="处理说明">
            <n-input v-model:value="reviewModal.form.handleResult" type="textarea" :rows="4" placeholder="可填写处理原因" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="reviewModal.show = false">取消</n-button>
            <n-button type="primary" :loading="reviewing" @click="submitReview">提交审核</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NInputNumber, NModal, NPagination, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import { adminReportPage, reviewAdminReport } from '@/api/admin'

const message = useMessage()
const loading = ref(false)
const reviewing = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  reporterId: null,
  reporterKeyword: '',
  targetType: null,
  status: null
})

const page = reactive({
  total: 0,
  records: []
})

const targetTypeOptions = [
  { label: '商品', value: 'goods' },
  { label: '用户', value: 'user' }
]

const statusOptions = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' }
]

const reviewStatusOptions = [
  { label: '通过', value: 'approved' },
  { label: '驳回', value: 'rejected' }
]

const reviewModal = reactive({
  show: false,
  reportId: null,
  form: {
    status: 'approved',
    handleResult: ''
  }
})

const statusLabel = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已驳回'
}

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminReportPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载举报失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const openReview = (row) => {
  reviewModal.reportId = row.id
  reviewModal.form.status = 'approved'
  reviewModal.form.handleResult = row.handleResult || ''
  reviewModal.show = true
}

const submitReview = async () => {
  if (!reviewModal.reportId) return
  try {
    reviewing.value = true
    await reviewAdminReport(reviewModal.reportId, reviewModal.form)
    reviewModal.show = false
    message.success('审核完成')
    await loadList()
  } catch (error) {
    message.error(error.message || '审核失败')
  } finally {
    reviewing.value = false
  }
}

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  {
    title: '举报人',
    key: 'reporter',
    width: 200,
    render: (row) => `${row.reporterName || '未知用户'} (ID:${row.reporterId})`
  },
  { title: '对象类型', key: 'targetType', width: 90 },
  { title: '对象ID', key: 'targetId', width: 90 },
  { title: '原因', key: 'reason', width: 120 },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h(NTag, { type: row.status === 'pending' ? 'warning' : row.status === 'approved' ? 'success' : 'default' }, { default: () => statusLabel[row.status] || row.status })
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => row.status === 'pending' ? h(NButton, { size: 'small', type: 'primary', onClick: () => openReview(row) }, { default: () => '审核' }) : null
  }
]

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr 1fr auto;
  gap: 12px;
  margin-bottom: 14px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .toolbar {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
