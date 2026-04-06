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

      <n-modal
        v-model:show="detailModal.show"
        to="body"
        preset="card"
        title="商品详情"
        style="width: 920px"
        :mask-closable="true"
        @after-leave="closeDetailModal"
      >
        <n-spin :show="detailModal.loading">
          <div v-if="detailModal.data" class="detail-wrap">
            <div class="detail-hero">
              <img :src="resolveFileUrl(detailModal.data.mainImage) || GOODS_IMAGE_PLACEHOLDER" :alt="detailModal.data.title" />
            </div>

            <n-descriptions label-placement="left" bordered :column="2" size="small">
              <n-descriptions-item label="商品ID">{{ detailModal.data.id }}</n-descriptions-item>
              <n-descriptions-item label="卖家">{{ detailModal.data.userName || `ID:${detailModal.data.userId}` }}</n-descriptions-item>
              <n-descriptions-item label="分类ID">{{ detailModal.data.categoryId }}</n-descriptions-item>
              <n-descriptions-item label="状态">{{ statusLabelMap[detailModal.data.status] || detailModal.data.status }}</n-descriptions-item>
              <n-descriptions-item label="价格">¥{{ detailModal.data.price }}</n-descriptions-item>
              <n-descriptions-item label="原价">{{ detailModal.data.originalPrice ? `¥${detailModal.data.originalPrice}` : '-' }}</n-descriptions-item>
              <n-descriptions-item label="成色">{{ detailModal.data.conditionLevel || '-' }}</n-descriptions-item>
              <n-descriptions-item label="联系方式">{{ detailModal.data.contactInfo || '-' }}</n-descriptions-item>
              <n-descriptions-item label="交易地点">{{ detailModal.data.tradeLocation || '-' }}</n-descriptions-item>
              <n-descriptions-item label="浏览量">{{ detailModal.data.viewCount || 0 }}</n-descriptions-item>
              <n-descriptions-item label="收藏量">{{ detailModal.data.favoriteCount || 0 }}</n-descriptions-item>
              <n-descriptions-item label="创建时间">{{ detailModal.data.createTime || '-' }}</n-descriptions-item>
            </n-descriptions>

            <div class="detail-section">
              <div class="section-title">商品描述</div>
              <div class="detail-description">{{ detailModal.data.description || '暂无描述' }}</div>
            </div>

            <div class="detail-section">
              <div class="section-title">商品图片</div>
              <div v-if="detailModal.data.imageList && detailModal.data.imageList.length" class="image-grid">
                <img v-for="(img, index) in detailModal.data.imageList" :key="`${detailModal.data.id}-${index}`" :src="resolveFileUrl(img) || GOODS_IMAGE_PLACEHOLDER" alt="商品图片" />
              </div>
              <n-empty v-else description="暂无商品图片" />
            </div>
          </div>
          <n-empty v-else description="暂无详情数据" />
        </n-spin>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NCard, NDataTable, NDescriptions, NDescriptionsItem, NEmpty, NInput, NInputNumber, NPagination, NSelect, NSpin, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { adminGoodsDetail, adminGoodsPage, deleteAdminGoods, updateAdminGoodsStatus } from '@/api/admin'
import { resolveFileUrl } from '@/utils/file'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const GOODS_IMAGE_PLACEHOLDER =
  "data:image/svg+xml;charset=UTF-8,<svg xmlns='http://www.w3.org/2000/svg' width='600' height='420' viewBox='0 0 600 420'><rect width='600' height='420' fill='%23f3f4f6'/><text x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-size='28' font-family='Arial'>暂无图片</text></svg>"

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

const detailModal = reactive({
  show: false,
  loading: false,
  data: null
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

const openDetail = async (row) => {
  detailModal.show = true
  detailModal.loading = true
  detailModal.data = null

  try {
    detailModal.data = await adminGoodsDetail(row.id)
  } catch (error) {
    message.error(error.message || '加载详情失败')
    detailModal.show = false
  } finally {
    detailModal.loading = false
  }
}

const closeDetailModal = () => {
  detailModal.show = false
  detailModal.loading = false
  detailModal.data = null
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
    width: 340,
    render: (row) =>
      h(
        NSpace,
        { size: 6 },
        {
          default: () => [
            h(
              NButton,
              { size: 'small', onClick: () => openDetail(row) },
              { default: () => '查看详情' }
            ),
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

.detail-wrap {
  display: grid;
  gap: 16px;
}

.detail-hero img {
  width: 100%;
  max-height: 280px;
  object-fit: cover;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.detail-section {
  display: grid;
  gap: 10px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.detail-description {
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.image-grid img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

@media (max-width: 900px) {
  .toolbar {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
