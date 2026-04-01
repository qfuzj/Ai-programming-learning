<template>
  <div class="admin-page">
    <n-card title="订单管理" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.orderNo" placeholder="订单号" clearable />
        <n-input-number v-model:value="query.buyerId" :min="1" placeholder="买家ID" clearable />
        <n-input v-model:value="query.buyerKeyword" placeholder="买家姓名关键词" clearable />
        <n-input-number v-model:value="query.sellerId" :min="1" placeholder="卖家ID" clearable />
        <n-input v-model:value="query.sellerKeyword" placeholder="卖家姓名关键词" clearable />
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
import { adminOrderPage, closeAdminOrder } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  buyerId: null,
  buyerKeyword: '',
  sellerId: null,
  sellerKeyword: '',
  status: null
})

const page = reactive({
  total: 0,
  records: []
})

const statusOptions = [
  { label: '待确认', value: 'pending' },
  { label: '已确认', value: 'confirmed' },
  { label: '交易中', value: 'trading' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
  { label: '已关闭', value: 'closed' }
]

const statusLabel = {
  pending: '待确认',
  confirmed: '已确认',
  trading: '交易中',
  completed: '已完成',
  cancelled: '已取消',
  closed: '已关闭'
}

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminOrderPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const handleClose = (row) => {
  dialog.warning({
    title: '确认关闭',
    content: `确认关闭订单 ${row.orderNo} 吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await closeAdminOrder(row.id)
        message.success('订单已关闭')
        await loadList()
      } catch (error) {
        message.error(error.message || '操作失败')
      }
    }
  })
}

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '订单号', key: 'orderNo', width: 180 },
  { title: '商品ID', key: 'goodsId', width: 90 },
  {
    title: '买家',
    key: 'buyer',
    width: 180,
    render: (row) => `${row.buyerName || '未知用户'} (ID:${row.buyerId})`
  },
  {
    title: '卖家',
    key: 'seller',
    width: 180,
    render: (row) => `${row.sellerName || '未知用户'} (ID:${row.sellerId})`
  },
  { title: '成交价', key: 'dealPrice', width: 90 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h(NTag, null, { default: () => statusLabel[row.status] || row.status })
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          row.status !== 'closed' && row.status !== 'completed'
            ? h(NButton, { size: 'small', type: 'warning', onClick: () => handleClose(row) }, { default: () => '关闭' })
            : null
        ]
      })
  }
]

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1.2fr 1fr 1.2fr 1fr auto;
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
