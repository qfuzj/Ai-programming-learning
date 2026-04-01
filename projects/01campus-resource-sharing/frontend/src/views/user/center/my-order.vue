<template>
  <div class="my-order-page">
    <n-card title="我的订单" :bordered="false">
      <n-tabs v-model:value="tab" @update:value="handleTabChange">
        <n-tab-pane name="buyer" tab="我买到的" />
        <n-tab-pane name="seller" tab="我卖出的" />
      </n-tabs>

      <div v-if="page.records.length" class="list">
        <div class="item" v-for="item in page.records" :key="item.id">
          <img class="thumb" :src="resolveFileUrl(item.goodsMainImage)" alt="thumb" @click="goDetail(item.goodsId)" />
          <div class="content">
            <div class="title" @click="goDetail(item.goodsId)">{{ item.goodsTitle || '商品已删除' }}</div>
            <div class="meta">
              <span>订单号：{{ item.orderNo }}</span>
              <span>价格：¥{{ item.dealPrice }}</span>
              <span>状态：{{ statusText(item.status) }}</span>
            </div>
            <div class="meta sub">
              <span>买家：{{ item.buyerName || item.buyerId }}</span>
              <span>卖家：{{ item.sellerName || item.sellerId }}</span>
            </div>
            <div class="meta sub" v-if="item.remark">备注：{{ item.remark }}</div>
            <div class="meta sub">创建时间：{{ formatDate(item.createTime) }}</div>
          </div>
          <div class="actions">
            <n-button size="small" @click="goDetail(item.goodsId)">查看商品</n-button>
            <n-button
              v-if="tab === 'buyer' && ['pending', 'confirmed'].includes(item.status)"
              size="small"
              type="warning"
              @click="handleCancel(item.id)"
            >取消订单</n-button>
            <n-button
              v-if="tab === 'buyer' && ['confirmed', 'trading'].includes(item.status)"
              size="small"
              type="primary"
              @click="handleComplete(item.id)"
            >完成订单</n-button>
            <n-button
              v-if="tab === 'buyer' && item.status === 'completed'"
              size="small"
              type="success"
              @click="openComment(item)"
            >评价订单</n-button>
            <n-button
              v-if="tab === 'seller' && item.status === 'pending'"
              size="small"
              type="primary"
              @click="handleConfirm(item.id)"
            >确认订单</n-button>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无订单" />

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

      <n-modal v-model:show="commentModal.show" preset="card" title="订单评价" style="width: 520px">
        <div class="comment-form">
          <div class="label">评分</div>
          <n-rate v-model:value="commentForm.score" />
          <div class="label" style="margin-top: 12px">评价内容</div>
          <n-input
            v-model:value="commentForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-count
            placeholder="请输入你的评价"
          />
        </div>
        <template #footer>
          <n-space justify="end">
            <n-button @click="commentModal.show = false">取消</n-button>
            <n-button type="primary" :loading="commentSubmitting" @click="submitComment">提交评价</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NInput, NModal, NPagination, NRate, NSpace, NTabPane, NTabs, useMessage } from 'naive-ui'
import { addComment } from '@/api/comment'
import { buyerOrderList, cancelOrder, completeOrder, confirmOrder, sellerOrderList } from '@/api/order'
import { resolveFileUrl } from '@/utils/file'

const router = useRouter()
const message = useMessage()

const tab = ref('buyer')
const query = reactive({
  pageNum: 1,
  pageSize: 10
})

const page = reactive({
  total: 0,
  records: []
})

const commentModal = reactive({
  show: false,
  order: null
})
const commentForm = reactive({
  score: 5,
  content: ''
})
const commentSubmitting = ref(false)

const statusMap = {
  pending: '待确认',
  confirmed: '已确认',
  trading: '交易中',
  completed: '已完成',
  cancelled: '已取消',
  closed: '已关闭'
}

const statusText = (status) => statusMap[status] || status

const loadList = async () => {
  try {
    const api = tab.value === 'buyer' ? buyerOrderList : sellerOrderList
    const res = await api(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载订单失败')
  }
}

const handleTabChange = () => {
  query.pageNum = 1
  loadList()
}

const handleCancel = async (id) => {
  try {
    await cancelOrder(id)
    message.success('取消成功')
    await loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const handleConfirm = async (id) => {
  try {
    await confirmOrder(id)
    message.success('确认成功')
    await loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const handleComplete = async (id) => {
  try {
    await completeOrder(id)
    message.success('订单已完成')
    await loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const openComment = (order) => {
  commentModal.order = order
  commentForm.score = 5
  commentForm.content = ''
  commentModal.show = true
}

const submitComment = async () => {
  if (!commentModal.order) return
  try {
    commentSubmitting.value = true
    await addComment({
      orderId: commentModal.order.id,
      goodsId: commentModal.order.goodsId,
      toUserId: commentModal.order.sellerId,
      score: commentForm.score,
      content: commentForm.content?.trim() || undefined
    })
    commentModal.show = false
    message.success('评价成功')
  } catch (error) {
    message.error(error.message || '评价失败')
  } finally {
    commentSubmitting.value = false
  }
}

const goDetail = (goodsId) => {
  if (!goodsId) return
  router.push(`/goods/${goodsId}`)
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(loadList)
</script>

<style scoped>
.my-order-page {
  max-width: 1000px;
  margin: 0 auto;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  gap: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
}

.thumb {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  background: #f3f3f3;
  cursor: pointer;
}

.content {
  min-width: 0;
}

.title {
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.title:hover {
  color: #d48806;
}

.meta {
  margin-top: 8px;
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #666;
  flex-wrap: wrap;
}

.meta.sub {
  margin-top: 6px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.comment-form .label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

@media (max-width: 760px) {
  .item {
    grid-template-columns: 1fr;
  }

  .thumb {
    width: 100%;
    height: 180px;
  }

  .actions {
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>
