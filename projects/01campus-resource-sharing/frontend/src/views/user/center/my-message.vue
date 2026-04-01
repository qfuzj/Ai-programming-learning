<template>
  <div class="my-message-page">
    <n-card title="我的消息" :bordered="false">
      <div v-if="page.records.length" class="list">
        <div class="item" v-for="item in page.records" :key="item.id" :class="{ unread: !item.mine && item.isRead === 0 }">
          <img class="thumb" :src="resolveFileUrl(item.goodsMainImage)" alt="thumb" @click="goDetail(item.goodsId)" />
          <div class="content">
            <div class="title" @click="goDetail(item.goodsId)">{{ item.goodsTitle || '商品已删除' }}</div>
            <div class="meta">
              <span>{{ item.mine ? '我发送给' : '来自' }}：{{ item.mine ? item.receiverName : item.senderName }}</span>
              <span>类型：{{ item.messageType }}</span>
              <span>{{ formatDate(item.createTime) }}</span>
            </div>
            <div class="text">{{ item.content }}</div>
            <div class="reply-box" v-if="item.goodsId">
              <n-input
                v-model:value="replyMap[item.id]"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-count
                placeholder="回复对方..."
              />
              <div class="reply-actions">
                <n-button size="small" type="primary" :loading="replyingId === item.id" @click="reply(item)">回复</n-button>
              </div>
            </div>
          </div>
          <div class="actions">
            <n-button size="small" @click="goDetail(item.goodsId)">查看商品</n-button>
            <n-button v-if="!item.mine && item.isRead === 0" size="small" type="primary" @click="markRead(item.id)">标记已读</n-button>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无消息" />

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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NInput, NPagination, useMessage } from 'naive-ui'
import { listMessages, readMessage, sendMessage } from '@/api/message'
import { resolveFileUrl } from '@/utils/file'

const router = useRouter()
const message = useMessage()

const query = reactive({
  pageNum: 1,
  pageSize: 10
})

const page = reactive({
  total: 0,
  records: []
})

const replyMap = reactive({})
const replyingId = ref(null)

const loadList = async () => {
  try {
    const res = await listMessages(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载消息失败')
  }
}

const markRead = async (id) => {
  try {
    await readMessage(id)
    await loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const reply = async (item) => {
  const content = (replyMap[item.id] || '').trim()
  if (!content) {
    message.warning('请输入回复内容')
    return
  }
  const receiverId = item.mine ? item.receiverId : item.senderId
  if (!receiverId) {
    message.warning('无法识别接收人')
    return
  }

  try {
    replyingId.value = item.id
    await sendMessage({
      goodsId: item.goodsId,
      receiverId,
      content,
      messageType: 'consult'
    })
    replyMap[item.id] = ''
    message.success('回复成功')
    await loadList()
  } catch (error) {
    message.error(error.message || '回复失败')
  } finally {
    replyingId.value = null
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
.my-message-page {
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
  grid-template-columns: 96px 1fr auto;
  gap: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
}

.item.unread {
  border-color: #ffd666;
  background: #fffbe6;
}

.thumb {
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  background: #f3f3f3;
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
  color: #666;
  font-size: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.text {
  margin-top: 10px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
}

.reply-box {
  margin-top: 10px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
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
