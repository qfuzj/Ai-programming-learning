<template>
  <div class="admin-page">
    <n-card title="评价管理" :bordered="false">
      <div class="toolbar">
        <n-input-number v-model:value="query.goodsId" :min="1" placeholder="商品ID" />
        <n-input-number v-model:value="query.fromUserId" :min="1" placeholder="评价人ID" />
        <n-input v-model:value="query.fromUserKeyword" placeholder="评价人姓名关键词" clearable />
        <n-input-number v-model:value="query.toUserId" :min="1" placeholder="被评人ID" />
        <n-input v-model:value="query.toUserKeyword" placeholder="被评人姓名关键词" clearable />
        <n-select v-model:value="query.score" :options="scoreOptions" clearable placeholder="评分" />
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
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NInput, NInputNumber, NPagination, NSelect, useDialog, useMessage } from 'naive-ui'
import { adminCommentPage, deleteAdminComment } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  goodsId: null,
  fromUserId: null,
  fromUserKeyword: '',
  toUserId: null,
  toUserKeyword: '',
  score: null
})

const page = reactive({
  total: 0,
  records: []
})

const scoreOptions = [
  { label: '1星', value: 1 },
  { label: '2星', value: 2 },
  { label: '3星', value: 3 },
  { label: '4星', value: 4 },
  { label: '5星', value: 5 }
]

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminCommentPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载评价失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const handleDelete = (row) => {
  dialog.error({
    title: '确认删除',
    content: `确认删除评价 #${row.id} 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAdminComment(row.id)
        message.success('删除成功')
        await loadList()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '订单ID', key: 'orderId', width: 90 },
  { title: '商品ID', key: 'goodsId', width: 90 },
  {
    title: '评价人',
    key: 'fromUser',
    width: 180,
    render: (row) => `${row.fromUserName || '未知用户'} (ID:${row.fromUserId})`
  },
  {
    title: '被评人',
    key: 'toUser',
    width: 180,
    render: (row) => `${row.toUserName || '未知用户'} (ID:${row.toUserId})`
  },
  { title: '评分', key: 'score', width: 70 },
  { title: '内容', key: 'content', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
  }
]

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1000px) {
  .toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
