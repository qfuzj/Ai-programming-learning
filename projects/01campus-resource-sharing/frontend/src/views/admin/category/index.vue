<template>
  <div class="admin-page">
    <n-card title="分类管理" :bordered="false">
      <div class="toolbar">
        <n-button type="primary" @click="openModal()">新增分类</n-button>
      </div>

      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" />

      <n-modal v-model:show="modal.show" preset="card" :title="modal.form.id ? '编辑分类' : '新增分类'" style="width: 460px">
        <n-form :model="modal.form" label-placement="top">
          <n-form-item label="分类名称">
            <n-input v-model:value="modal.form.name" placeholder="请输入分类名称" />
          </n-form-item>
          <n-form-item label="排序">
            <n-input-number v-model:value="modal.form.sort" :min="0" style="width: 100%" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="modal.form.status" :options="statusOptions" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="modal.show = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="saveCategory">保存</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NInputNumber, NModal, NSelect, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { addAdminCategory, adminCategoryList, deleteAdminCategory, updateAdminCategory, updateAdminCategoryStatus } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)

const list = ref([])
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const modal = reactive({
  show: false,
  form: {
    id: null,
    name: '',
    sort: 0,
    status: 1
  }
})

const loadList = async () => {
  try {
    loading.value = true
    list.value = (await adminCategoryList()) || []
  } catch (error) {
    message.error(error.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

const openModal = (row) => {
  modal.form.id = row?.id || null
  modal.form.name = row?.name || ''
  modal.form.sort = row?.sort ?? 0
  modal.form.status = row?.status ?? 1
  modal.show = true
}

const saveCategory = async () => {
  if (!modal.form.name.trim()) {
    message.warning('请输入分类名称')
    return
  }

  try {
    saving.value = true
    const payload = {
      id: modal.form.id,
      name: modal.form.name,
      sort: modal.form.sort,
      status: modal.form.status
    }
    if (modal.form.id) {
      await updateAdminCategory(payload)
    } else {
      await addAdminCategory(payload)
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
  dialog.warning({
    title: '确认操作',
    content: `确认将分类${row.name}设为${nextStatus === 1 ? '启用' : '禁用'}吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await updateAdminCategoryStatus(row.id, nextStatus)
        message.success('操作成功')
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
    content: `确认删除分类${row.name}吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAdminCategory(row.id)
        message.success('删除成功')
        await loadList()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '分类名称', key: 'name' },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
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
  margin-bottom: 14px;
}
</style>
