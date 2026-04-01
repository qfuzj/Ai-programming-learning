<template>
  <div class="my-favorite-page">
    <n-card title="我的收藏" :bordered="false">
      <div v-if="page.records.length" class="list">
        <div class="item" v-for="item in page.records" :key="item.favoriteId">
          <img class="thumb" :src="resolveFileUrl(item.mainImage)" alt="thumb" @click="goDetail(item.goodsId)" />
          <div class="content">
            <div class="title" @click="goDetail(item.goodsId)">{{ item.title }}</div>
            <div class="meta">
              <span class="price">¥{{ item.price }}</span>
              <span>成色：{{ item.conditionLevel || '-' }}</span>
              <span>状态：{{ item.status }}</span>
            </div>
            <div class="meta sub">收藏时间：{{ formatDate(item.favoriteTime) }}</div>
          </div>
          <div class="actions">
            <n-button size="small" @click="goDetail(item.goodsId)">查看详情</n-button>
            <n-button size="small" type="warning" @click="cancelFavorite(item.goodsId)">取消收藏</n-button>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无收藏" />

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
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NPagination, useMessage } from 'naive-ui'
import { listFavorites, removeFavorite } from '@/api/favorite'
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

const loadList = async () => {
  try {
    const res = await listFavorites(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载收藏失败')
  }
}

const cancelFavorite = async (goodsId) => {
  try {
    await removeFavorite(goodsId)
    message.success('已取消收藏')
    await loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const goDetail = (id) => {
  router.push(`/goods/${id}`)
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
.my-favorite-page {
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
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
}

.thumb {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  background: #f3f3f3;
}

.content {
  min-width: 0;
}

.title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  cursor: pointer;
}

.title:hover {
  color: #d48806;
}

.meta {
  display: flex;
  gap: 12px;
  color: #666;
  font-size: 13px;
  flex-wrap: wrap;
}

.meta.sub {
  margin-top: 10px;
}

.price {
  color: #d93025;
  font-weight: 700;
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
