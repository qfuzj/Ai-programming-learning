<template>
  <div class="home-container">
    <section class="hero-wrap">
      <aside class="category-rail">
        <h3>全部分类</h3>
        <div class="category-item" v-for="item in categories" :key="item.id" @click="navigateToGoods(item.id)">
          <n-icon><GridOutline /></n-icon>
          <span>{{ item.name }}</span>
        </div>
      </aside>

      <section class="promo-board">
        <div class="banner-card">
          <n-carousel v-if="banners.length" autoplay :interval="4000" dot-type="line" class="hero-carousel">
            <n-carousel-item v-for="banner in banners" :key="banner.id">
              <div class="banner-item">
                <img :src="resolveFileUrl(banner.imageUrl)" :alt="banner.title" />
                <div class="banner-shadow"></div>
                <div class="banner-title">{{ banner.title || '校园精选好物' }}</div>
              </div>
            </n-carousel-item>
          </n-carousel>
          <div v-else class="banner-empty">暂无轮播内容</div>
        </div>

        <div class="quick-grid">
          <article class="quick-card yellow" @click="navigateToGoods()">
            <h4>今日捡漏</h4>
            <p>低价优选 · 限时发现</p>
            <span>去看看</span>
          </article>
          <article class="quick-card cyan" @click="navigateToGoods()">
            <h4>数码专区</h4>
            <p>手机平板 · 电脑配件</p>
            <span>去看看</span>
          </article>
          <article class="quick-card green" @click="navigateToGoods()">
            <h4>毕业季特卖</h4>
            <p>宿舍搬家 · 快速转手</p>
            <span>去看看</span>
          </article>
          <article class="quick-card pink" @click="navigateToGoods()">
            <h4>省钱卡券</h4>
            <p>吃喝玩乐 · 实用优惠</p>
            <span>去看看</span>
          </article>
        </div>
      </section>
    </section>

    <section class="notice-section" v-if="notices.length">
      <n-icon size="18"><MegaphoneOutline /></n-icon>
      <div class="notice-list">
        <div class="notice-item" v-for="notice in notices.slice(0, 5)" :key="notice.id">
          <span class="dot"></span>
          <span class="title">{{ notice.title }}</span>
          <span class="time">{{ formatDate(notice.createTime) }}</span>
        </div>
      </div>
    </section>

    <section class="goods-section">
      <div class="section-head">
        <h3>猜你喜欢</h3>
        <n-button text @click="navigateToGoods()">查看更多</n-button>
      </div>

      <div class="interest-tabs">
        <button
          v-for="item in interestTabs"
          :key="item.key"
          class="interest-pill"
          :class="{ active: activeTabKey === item.key }"
          @click="activeTabKey = item.key"
        >
          {{ item.label }}
        </button>
      </div>

      <div v-if="displayedGoods.length" class="goods-grid">
        <div class="goods-card" v-for="goods in displayedGoods" :key="`${activeTabKey}-${goods.id}`" @click="navigateToDetail(goods.id)">
          <div class="goods-image">
            <img
              :src="resolveFileUrl(goods.mainImage) || GOODS_IMAGE_PLACEHOLDER"
              :alt="goods.title"
              @error="handleGoodsImageError"
            />
            <div class="condition">{{ goods.conditionLevel || '成色良好' }}</div>
          </div>
          <div class="goods-info">
            <div class="goods-title">{{ goods.title }}</div>
            <div class="goods-price">¥{{ goods.price }}</div>
            <div class="goods-meta">
              <span><n-icon><EyeOutline /></n-icon>{{ goods.viewCount || 0 }}</span>
              <span><n-icon><HeartOutline /></n-icon>{{ goods.favoriteCount || 0 }}</span>
              <span><n-icon><TimeOutline /></n-icon>{{ formatDate(goods.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无推荐商品" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCarousel, NCarouselItem, NEmpty, NIcon, useMessage } from 'naive-ui'
import { EyeOutline, GridOutline, HeartOutline, MegaphoneOutline, TimeOutline } from '@vicons/ionicons5'
import { getBanners, getCategories, getLatest, getNotices, getRecommend } from '@/api/home'
import { resolveFileUrl } from '@/utils/file'

const router = useRouter()
const message = useMessage()

const banners = ref([])
const recommend = ref([])
const latest = ref([])
const notices = ref([])
const categories = ref([])
const activeTabKey = ref('recommend')
const GOODS_IMAGE_PLACEHOLDER =
  "data:image/svg+xml;charset=UTF-8,<svg xmlns='http://www.w3.org/2000/svg' width='600' height='420' viewBox='0 0 600 420'><rect width='600' height='420' fill='%23f3f4f6'/><text x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-size='28' font-family='Arial'>暂无图片</text></svg>"

const interestTabs = computed(() => {
  const dynamic = categories.value.slice(0, 8).map((item) => ({
    key: `category-${item.id}`,
    label: item.name,
    type: 'category',
    categoryId: item.id,
    categoryName: item.name
  }))
  return [
    { key: 'recommend', label: '猜你喜欢', type: 'recommend' },
    { key: 'latest', label: '最新发布', type: 'latest' },
    ...dynamic
  ]
})

const mergedGoods = computed(() => {
  const map = new Map()
  ;[...recommend.value, ...latest.value].forEach((item) => {
    if (!map.has(item.id)) {
      map.set(item.id, item)
    }
  })
  return Array.from(map.values())
})

const displayedGoods = computed(() => {
  const currentTab = interestTabs.value.find((item) => item.key === activeTabKey.value)
  if (!currentTab || currentTab.type === 'recommend') {
    return recommend.value
  }
  if (currentTab.type === 'latest') {
    return latest.value
  }

  const filtered = mergedGoods.value.filter((item) => {
    return Number(item.categoryId) === Number(currentTab.categoryId)
  })

  return filtered.slice(0, 20)
})

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const handleGoodsImageError = (event) => {
  const target = event?.target
  if (target && target.src !== GOODS_IMAGE_PLACEHOLDER) {
    target.src = GOODS_IMAGE_PLACEHOLDER
  }
}

const loadData = async () => {
  try {
    const [bannersRes, recommendRes, latestRes, noticesRes, categoriesRes] = await Promise.all([
      getBanners(),
      getRecommend(),
      getLatest(),
      getNotices(),
      getCategories()
    ])

    banners.value = bannersRes || []
    recommend.value = recommendRes || []
    latest.value = latestRes || []
    notices.value = noticesRes || []
    categories.value = categoriesRes || []
  } catch (error) {
    message.error('加载首页数据失败')
    console.error(error)
  }
}

const navigateToGoods = (categoryId) => {
  const query = categoryId ? { categoryId } : {}
  router.push({ path: '/goods', query })
}

const navigateToDetail = (goodsId) => {
  router.push(`/goods/${goodsId}`)
}

onMounted(loadData)
</script>

<style scoped>
.home-container {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  gap: 18px;
}

.hero-wrap {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 18px;
}

.category-rail {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 14px;
}

.category-rail h3 {
  margin: 4px 0 10px;
  font-size: 16px;
  color: #111827;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 10px;
  padding: 8px;
  color: #374151;
  cursor: pointer;
  transition: 0.2s;
}

.category-item:hover {
  background: #fff;
  color: #111827;
}

.banner-card {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.hero-carousel,
.banner-item {
  height: 220px;
}

.banner-item {
  position: relative;
}

.banner-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-shadow {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 30%, rgba(0, 0, 0, 0.55) 100%);
}

.banner-title {
  position: absolute;
  left: 16px;
  bottom: 14px;
  font-size: 22px;
  font-weight: 800;
  color: #fff;
}

.banner-empty {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.quick-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-card {
  border-radius: 14px;
  padding: 14px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: 0.2s;
}

.quick-card:hover {
  transform: translateY(-2px);
}

.quick-card h4 {
  margin: 0;
  font-size: 18px;
}

.quick-card p {
  margin: 6px 0;
  color: #475569;
  font-size: 13px;
}

.quick-card span {
  display: inline-block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 700;
}

.quick-card.yellow {
  background: #fff7cc;
  border-color: #ffe071;
}

.quick-card.cyan {
  background: #dff8ff;
  border-color: #9ee6f9;
}

.quick-card.green {
  background: #dffce9;
  border-color: #97efb7;
}

.quick-card.pink {
  background: #ffe3f0;
  border-color: #f7b9d8;
}

.notice-section {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: grid;
  grid-template-columns: 28px 1fr;
  gap: 8px;
  align-items: center;
  padding: 12px;
  color: #475569;
}

.notice-list {
  display: grid;
  gap: 6px;
}

.notice-item {
  display: grid;
  grid-template-columns: 12px 1fr auto;
  align-items: center;
  gap: 8px;
}

.notice-item .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f97316;
}

.notice-item .title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-item .time {
  font-size: 12px;
  color: #94a3b8;
}

.goods-section {
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 14px;
}

.interest-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.interest-pill {
  border: 0;
  cursor: pointer;
  border-radius: 999px;
  background: #f1f5f9;
  color: #334155;
  font-size: 14px;
  padding: 8px 14px;
  font-weight: 600;
}

.interest-pill.active {
  background: #ffe30a;
  color: #111827;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.goods-card {
  border: 1px solid #eef2f7;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.2s;
  background: #fff;
}

.goods-card:hover {
  border-color: #ffd84f;
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.14);
  transform: translateY(-2px);
}

.goods-image {
  height: 160px;
  position: relative;
  background: #f3f4f6;
}

.goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.condition {
  position: absolute;
  top: 8px;
  left: 8px;
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 12px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
}

.goods-info {
  padding: 10px;
}

.goods-title {
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.goods-price {
  margin-top: 8px;
  color: #ef4444;
  font-size: 20px;
  font-weight: 800;
}

.goods-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #64748b;
}

.goods-meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

@media (max-width: 1100px) {
  .hero-wrap {
    grid-template-columns: 1fr;
  }

  .goods-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
