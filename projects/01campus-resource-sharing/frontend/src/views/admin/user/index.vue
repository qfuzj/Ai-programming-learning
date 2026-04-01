<template>
  <div class="admin-page">
    <n-card title="用户管理" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.keyword" placeholder="用户名/昵称" clearable />
        <n-select v-model:value="query.role" :options="roleOptions" clearable placeholder="角色" />
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
import { NButton, NCard, NDataTable, NInput, NPagination, NSelect, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { adminUserPage, updateAdminUserStatus } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  role: null,
  status: null
})

const page = reactive({
  total: 0,
  records: []
})

const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' }
]

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

const roleLabel = (role) => (role === 'admin' ? '管理员' : '普通用户')
const statusLabel = (status) => (status === 1 ? '正常' : '禁用')

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminUserPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const handleStatus = (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  dialog.warning({
    title: '确认操作',
    content: `确认将用户${row.username}设为${nextStatus === 1 ? '正常' : '禁用'}吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await updateAdminUserStatus(row.id, nextStatus)
        message.success('操作成功')
        await loadList()
      } catch (error) {
        message.error(error.message || '操作失败')
      }
    }
  })
}

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '角色', key: 'role', render: (row) => h(NTag, { type: row.role === 'admin' ? 'warning' : 'default' }, { default: () => roleLabel(row.role) }) },
  { title: '状态', key: 'status', render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'error' }, { default: () => statusLabel(row.status) }) },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row) =>
      h(
        NSpace,
        null,
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                type: row.status === 1 ? 'warning' : 'success',
                onClick: () => handleStatus(row)
              },
              { default: () => (row.status === 1 ? '禁用' : '启用') }
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
  grid-template-columns: 1.4fr 1fr 1fr auto;
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
