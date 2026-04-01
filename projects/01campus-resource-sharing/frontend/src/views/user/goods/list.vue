<template>
  <div class="goods-list-page">
    <n-card title="商品广场" :bordered="false">
      <div class="filters">
        <n-input v-model:value="query.keyword" placeholder="关键词" clearable />
        <n-select v-model:value="query.categoryId" :options="categoryOptions" clearable placeholder="分类" />
        <n-input-number v-model:value="query.minPrice" :min="0" placeholder="最低价" />
        <n-input-number v-model:value="query.maxPrice" :min="0" placeholder="最高价" />
        <n-input v-model:value="query.conditionLevel" placeholder="成色，如9成新" clearable />
        <n-select v-model:value="query.sortType" :options="sortOptions" clearable placeholder="排序" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
        <n-button @click="handleReset">重置</n-button>
      </div>

      <div class="grid" v-if="page.records.length">
        <div
          class="card"
          v-for="item in page.records"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <img class="thumb" :src="resolveFileUrl(item.mainImage)" :alt="item.title" />
          <div class="title">{{ item.title }}</div>
          <div class="meta">
            <span class="price">¥{{ item.price }}</span>
            <span>{{ item.conditionLevel }}</span>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无商品" />

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
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NEmpty, NInput, NInputNumber, NPagination, NSelect, useMessage } from 'naive-ui'
import { listGoods } from '@/api/goods'
import { getCategories } from '@/api/home'
import { resolveFileUrl } from '@/utils/file'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  conditionLevel: '',
  sortType: 'latest'
})

const page = reactive({
  total: 0,
  records: []
})

const categoryOptions = ref([])
const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '价格升序', value: 'priceAsc' },
  { label: '价格降序', value: 'priceDesc' },
  { label: '最热', value: 'hot' }
]

const loadCategories = async () => {
  const categories = await getCategories()
  categoryOptions.value = (categories || []).map((item) => ({ label: item.name, value: item.id }))
}

const loadList = async () => {
  try {
    const res = await listGoods(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载商品失败')
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

const handleReset = () => {
  query.pageNum = 1
  query.pageSize = 10
  query.keyword = ''
  query.categoryId = null
  query.minPrice = null
  query.maxPrice = null
  query.conditionLevel = ''
  query.sortType = 'latest'
  loadList()
}

const goDetail = (id) => {
  router.push(`/goods/${id}`)
}

onMounted(async () => {
  if (route.query.keyword) {
    query.keyword = route.query.keyword
  }
  if (route.query.categoryId) {
    query.categoryId = Number(route.query.categoryId)
  }
  await loadCategories()
  await loadList()
})
</script>

<style scoped>
.goods-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
}

.card:hover {
  border-color: #ffe60f;
}

.thumb {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background: #f3f3f3;
}

.title {
  font-weight: 600;
  padding: 10px 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px 12px;
  color: #666;
  font-size: 13px;
}

.price {
  color: #d93025;
  font-weight: 700;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
