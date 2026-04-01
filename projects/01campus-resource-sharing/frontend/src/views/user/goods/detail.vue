<template>
  <div class="goods-detail-page">
    <n-card :title="detail.title || '商品详情'" :bordered="false">
      <div class="detail-layout" v-if="detail.id">
        <div class="left">
          <n-carousel v-if="images.length" :show-arrow="true" :autoplay="false" style="height: 360px">
            <n-carousel-item v-for="(img, idx) in images" :key="idx">
              <img class="detail-image" :src="resolveFileUrl(img)" alt="image" />
            </n-carousel-item>
          </n-carousel>
          <img v-else class="detail-image" :src="resolveFileUrl(detail.mainImage)" alt="main" />
        </div>
        <div class="right">
          <h2 class="title">{{ detail.title }}</h2>
          <div class="price">¥{{ detail.price }}</div>
          <div class="row">分类ID：{{ detail.categoryId }}</div>
          <div class="row">成色：{{ detail.conditionLevel }}</div>
          <div class="row">状态：{{ detail.status }}</div>
          <div class="row">收藏：{{ detail.favoriteCount }}，浏览：{{ detail.viewCount }}</div>
          <div class="row">地点：{{ detail.tradeLocation || '未填写' }}</div>
          <div class="row">联系方式：{{ detail.contactInfo }}</div>
          <div class="desc">{{ detail.description }}</div>
          <div class="actions">
            <n-button v-if="detail.owner" type="primary" @click="goEdit">编辑商品</n-button>
            <n-button v-else type="warning" :loading="favoriteLoading" @click="toggleFavorite">
              {{ detail.favorited ? '取消收藏' : '收藏商品' }}
            </n-button>
            <n-button
              v-if="!detail.owner && detail.status === 'on_sale'"
              type="primary"
              :loading="orderLoading"
              @click="placeOrder"
            >立即下单</n-button>
            <n-button v-if="!detail.owner" type="error" secondary @click="openReport">举报商品</n-button>
          </div>

          <div class="consult" v-if="!detail.owner">
            <div class="consult-title">留言咨询卖家</div>
            <n-input
              v-model:value="consultContent"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-count
              placeholder="请输入留言内容，例如：这个商品还在吗？"
            />
            <div class="consult-actions">
              <n-button type="primary" :loading="messageLoading" @click="sendConsult">发送留言</n-button>
            </div>
          </div>
        </div>
      </div>
      <n-empty v-else description="商品不存在或已删除" />

      <n-modal v-model:show="reportModal.show" preset="card" title="举报商品" style="width: 560px">
        <n-select v-model:value="reportForm.reason" :options="reportReasonOptions" placeholder="请选择举报原因" />
        <n-input
          v-model:value="reportForm.description"
          style="margin-top: 12px"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-count
          placeholder="请补充详细描述（选填）"
        />
        <template #footer>
          <n-space justify="end">
            <n-button @click="reportModal.show = false">取消</n-button>
            <n-button type="error" :loading="reportSubmitting" @click="submitReport">提交举报</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NCarousel, NCarouselItem, NEmpty, NInput, NModal, NSelect, NSpace, useMessage } from 'naive-ui'
import { getGoodsDetail } from '@/api/goods'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { sendMessage } from '@/api/message'
import { createOrder } from '@/api/order'
import { addReport } from '@/api/report'
import { resolveFileUrl } from '@/utils/file'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const detail = reactive({})
const consultContent = ref('')
const favoriteLoading = ref(false)
const messageLoading = ref(false)
const orderLoading = ref(false)
const reportSubmitting = ref(false)
const reportModal = reactive({ show: false })
const reportForm = reactive({
  reason: '',
  description: ''
})

const reportReasonOptions = [
  { label: '商品信息虚假', value: '商品信息虚假' },
  { label: '疑似违规内容', value: '疑似违规内容' },
  { label: '欺诈风险', value: '欺诈风险' },
  { label: '其他', value: '其他' }
]

const images = computed(() => {
  if (Array.isArray(detail.imageList) && detail.imageList.length) {
    return detail.imageList
  }
  return detail.mainImage ? [detail.mainImage] : []
})

const loadDetail = async () => {
  try {
    const data = await getGoodsDetail(route.params.id)
    Object.assign(detail, data || {})
  } catch (error) {
    message.error(error.message || '加载商品详情失败')
  }
}

const goEdit = () => {
  router.push(`/publish/${route.params.id}`)
}

const ensureLogin = () => {
  if (userStore.isLogin) {
    return true
  }
  message.warning('请先登录')
  router.push('/login')
  return false
}

const toggleFavorite = async () => {
  if (!ensureLogin()) return
  try {
    favoriteLoading.value = true
    if (detail.favorited) {
      await removeFavorite(detail.id)
      detail.favorited = false
      detail.favoriteCount = Math.max((detail.favoriteCount || 0) - 1, 0)
      message.success('已取消收藏')
    } else {
      await addFavorite(detail.id)
      detail.favorited = true
      detail.favoriteCount = (detail.favoriteCount || 0) + 1
      message.success('收藏成功')
    }
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const sendConsult = async () => {
  if (!ensureLogin()) return
  const content = consultContent.value.trim()
  if (!content) {
    message.warning('请输入留言内容')
    return
  }
  try {
    messageLoading.value = true
    await sendMessage({
      goodsId: detail.id,
      receiverId: detail.userId,
      content,
      messageType: 'consult'
    })
    consultContent.value = ''
    message.success('留言发送成功')
  } catch (error) {
    message.error(error.message || '留言发送失败')
  } finally {
    messageLoading.value = false
  }
}

const placeOrder = async () => {
  if (!ensureLogin()) return
  try {
    orderLoading.value = true
    await createOrder({
      goodsId: detail.id,
      remark: consultContent.value?.trim() || undefined
    })
    message.success('下单成功')
    router.push('/me/order')
  } catch (error) {
    message.error(error.message || '下单失败')
  } finally {
    orderLoading.value = false
  }
}

const openReport = () => {
  if (!ensureLogin()) return
  reportForm.reason = ''
  reportForm.description = ''
  reportModal.show = true
}

const submitReport = async () => {
  if (!reportForm.reason) {
    message.warning('请选择举报原因')
    return
  }
  try {
    reportSubmitting.value = true
    await addReport({
      targetType: 'goods',
      targetId: detail.id,
      reason: reportForm.reason,
      description: reportForm.description?.trim() || undefined
    })
    reportModal.show = false
    message.success('举报已提交')
  } catch (error) {
    message.error(error.message || '举报提交失败')
  } finally {
    reportSubmitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.goods-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 24px;
}

.detail-image {
  width: 100%;
  height: 360px;
  object-fit: cover;
  background: #f3f3f3;
  border-radius: 10px;
}

.title {
  margin: 0 0 10px;
}

.price {
  color: #d93025;
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 8px;
}

.row {
  margin: 6px 0;
  color: #444;
}

.desc {
  margin-top: 12px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.consult {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.consult-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.consult-actions {
  margin-top: 10px;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
