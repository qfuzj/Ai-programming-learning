<template>
  <div class="admin-page">
    <n-card title="轮播管理" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.keyword" placeholder="轮播标题关键词" clearable />
        <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="状态" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="openModal()">新增轮播</n-button>
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

      <n-modal v-model:show="modal.show" preset="card" :title="modal.form.id ? '编辑轮播' : '新增轮播'" style="width: 600px">
        <n-form :model="modal.form" label-placement="top">
          <n-form-item label="标题">
            <n-input v-model:value="modal.form.title" placeholder="可为空" />
          </n-form-item>
          <n-form-item label="图片地址">
            <n-input v-model:value="modal.form.imageUrl" placeholder="请输入图片 URL" />
          </n-form-item>
          <n-form-item label="跳转链接">
            <n-input v-model:value="modal.form.linkUrl" placeholder="可为空" />
          </n-form-item>
          <n-grid :cols="2" :x-gap="12">
            <n-form-item-gi label="排序">
              <n-input-number v-model:value="modal.form.sort" :min="0" style="width: 100%" />
            </n-form-item-gi>
            <n-form-item-gi label="状态">
              <n-select v-model:value="modal.form.status" :options="statusOptions" />
            </n-form-item-gi>
          </n-grid>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="modal.show = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="saveBanner">保存</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NFormItemGi, NGrid, NInput, NInputNumber, NModal, NPagination, NSelect, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { addAdminBanner, adminBannerPage, deleteAdminBanner, updateAdminBanner, updateAdminBannerStatus } from '@/api/admin'

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
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const modal = reactive({
  show: false,
  form: {
    id: null,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sort: 0,
    status: 1
  }
})

const loadList = async () => {
  try {
    loading.value = true
    const res = await adminBannerPage(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载轮播失败')
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
  modal.form.imageUrl = row?.imageUrl || ''
  modal.form.linkUrl = row?.linkUrl || ''
  modal.form.sort = row?.sort ?? 0
  modal.form.status = row?.status ?? 1
  modal.show = true
}

const saveBanner = async () => {
  if (!modal.form.imageUrl.trim()) {
    message.warning('请填写图片地址')
    return
  }

  try {
    saving.value = true
    const payload = { ...modal.form }
    if (payload.id) {
      await updateAdminBanner(payload)
    } else {
      await addAdminBanner(payload)
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
  updateAdminBannerStatus(row.id, nextStatus).then(() => {
    message.success('状态更新成功')
    loadList()
  }).catch((error) => {
    message.error(error.message || '操作失败')
  })
}

const handleDelete = (row) => {
  dialog.error({
    title: '确认删除',
    content: `确认删除轮播 #${row.id} 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAdminBanner(row.id)
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
    title: '图片',
    key: 'imageUrl',
    width: 120,
    render: (row) => h('img', { src: row.imageUrl, style: 'width: 90px;height: 40px;object-fit: cover;border-radius: 4px;' })
  },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openModal(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'warning', onClick: () => handleStatus(row) }, { default: () => (row.status === 1 ? '禁用' : '启用') }),
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
  grid-template-columns: 1.4fr 1fr auto auto;
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
