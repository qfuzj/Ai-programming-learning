<template>
  <div class="admin-page">
    <n-card title="公告管理" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.keyword" placeholder="公告标题关键词" clearable />
        <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="状态" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="openModal()">新增公告</n-button>
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

      <n-modal v-model:show="modal.show" preset="card" :title="modal.form.id ? '编辑公告' : '新增公告'" style="width: 560px">
        <n-form :model="modal.form" label-placement="top">
          <n-form-item label="标题">
            <n-input v-model:value="modal.form.title" />
          </n-form-item>
          <n-form-item label="内容">
            <n-input v-model:value="modal.form.content" type="textarea" :rows="5" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="modal.form.status" :options="statusOptions" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="modal.show = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="saveNotice">保存</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NModal, NPagination, NSelect, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { addAdminNotice, adminNoticePage, deleteAdminNotice, updateAdminNotice, updateAdminNoticeStatus } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null
})

const page = reactive({
  total: 0,
  records: []
})

const statusOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 }
]

const modal = reactive({
  show: false,
  form: {
    id: null,
    title: '',
    content: '',
    status: 1
  }
})

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminNoticePage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载公告失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const openModal = (row) => {
  modal.form.id = row?.id || null
  modal.form.title = row?.title || ''
  modal.form.content = row?.content || ''
  modal.form.status = row?.status ?? 1
  modal.show = true
}

const saveNotice = async () => {
  if (!modal.form.title.trim() || !modal.form.content.trim()) {
    message.warning('请填写完整公告信息')
    return
  }

  try {
    saving.value = true
    const payload = {
      id: modal.form.id,
      title: modal.form.title,
      content: modal.form.content,
      status: modal.form.status
    }
    if (modal.form.id) {
      await updateAdminNotice(payload)
    } else {
      await addAdminNotice(payload)
    }
    message.success('保存成功')
    modal.show = false
    await loadList()
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleStatus = (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  updateAdminNoticeStatus(row.id, nextStatus).then(() => {
    message.success('状态更新成功')
    loadList()
  }).catch((error) => {
    message.error(error.message || '操作失败')
  })
}

const handleDelete = (row) => {
  dialog.error({
    title: '确认删除',
    content: `确认删除公告《${row.title}》吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAdminNotice(row.id)
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
  { title: '内容', key: 'content', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '显示' : '隐藏') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openModal(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'warning', onClick: () => handleStatus(row) }, { default: () => (row.status === 1 ? '隐藏' : '显示') }),
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
        ]
      })
  }
]

onMounted(loadList)
</script>

<style scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1.6fr 1fr auto auto;
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
