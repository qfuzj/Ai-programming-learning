<template>
  <div class="admin-page">
    <n-card title="商品管理" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.keyword" placeholder="标题/描述关键词" clearable />
        <n-input-number v-model:value="query.userId" :min="1" placeholder="卖家ID" clearable />
        <n-input v-model:value="query.userKeyword" placeholder="卖家姓名关键词" clearable />
        <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="状态" />
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
import { NButton, NCard, NDataTable, NInput, NInputNumber, NPagination, NSelect, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { adminGoodsPage, deleteAdminGoods, updateAdminGoodsStatus } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  userId: null,
  userKeyword: '',
  status: null
})

const page = reactive({
  total: 0,
  records: []
})

const statusOptions = [
  { label: '在售', value: 'on_sale' },
  { label: '已下架', value: 'off_shelf' },
  { label: '已售出', value: 'sold' },
  { label: '待审核', value: 'pending' },
  { label: '已驳回', value: 'rejected' }
]

const statusLabelMap = {
  on_sale: '在售',
  off_shelf: '已下架',
  sold: '已售出',
  pending: '待审核',
  rejected: '已驳回'
}

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminGoodsPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const updateStatus = (row, status) => {
  dialog.warning({
    title: '确认操作',
    content: `确认将商品《${row.title}》状态改为 ${statusLabelMap[status] || status} 吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await updateAdminGoodsStatus(row.id, { status })
        message.success('状态更新成功')
        await loadList()
      } catch (error) {
        message.error(error.message || '操作失败')
      }
    }
  })
}

const handleDelete = (row) => {
  dialog.error({
    title: '确认删除',
    content: `确认删除商品《${row.title}》吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAdminGoods(row.id)
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
  { title: '标题', key: 'title' },
  {
    title: '卖家',
    key: 'seller',
    width: 180,
    render: (row) => `${row.userName || '未知用户'} (ID:${row.userId})`
  },
  { title: '价格', key: 'price', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render: (row) => h(NTag, { type: row.status === 'on_sale' ? 'success' : 'warning' }, { default: () => statusLabelMap[row.status] || row.status })
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render: (row) =>
      h(
        NSpace,
        { size: 6 },
        {
          default: () => [
            h(
              NButton,
              { size: 'small', onClick: () => updateStatus(row, 'on_sale') },
              { default: () => '上架' }
            ),
            h(
              NButton,
              { size: 'small', type: 'warning', onClick: () => updateStatus(row, 'off_shelf') },
              { default: () => '下架' }
            ),
            h(
              NButton,
              { size: 'small', type: 'error', onClick: () => handleDelete(row) },
              { default: () => '删除' }
            )
          ]
        }
      )
  }
]

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1.2fr 1fr auto;
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
