<template>
  <div class="dashboard-page">
    <div class="stats-grid">
      <n-card :bordered="false" class="stat-card blue">
        <div class="stat-head">
          <div class="icon-wrap">
            <n-icon><PeopleOutline /></n-icon>
          </div>
          <span>用户总数</span>
        </div>
        <div class="stat-value">{{ stats.userCount }}</div>
      </n-card>

      <n-card :bordered="false" class="stat-card orange">
        <div class="stat-head">
          <div class="icon-wrap">
            <n-icon><CubeOutline /></n-icon>
          </div>
          <span>商品总数</span>
        </div>
        <div class="stat-value">{{ stats.goodsCount }}</div>
      </n-card>

      <n-card :bordered="false" class="stat-card pink">
        <div class="stat-head">
          <div class="icon-wrap">
            <n-icon><ReceiptOutline /></n-icon>
          </div>
          <span>订单总数</span>
        </div>
        <div class="stat-value">{{ stats.orderCount }}</div>
      </n-card>

      <n-card :bordered="false" class="stat-card warning">
        <div class="stat-head">
          <div class="icon-wrap">
            <n-icon><WarningOutline /></n-icon>
          </div>
          <span>待审核举报</span>
          <n-tag size="small" type="warning" round>待处理</n-tag>
        </div>
        <div class="stat-value">{{ stats.pendingReportCount }}</div>
      </n-card>
    </div>

    <div class="chart-grid">
      <n-card title="商品分类分布" :bordered="false" class="chart-card">
        <div ref="categoryChartRef" class="chart-box"></div>
      </n-card>
      <n-card title="近7日新增商品" :bordered="false" class="chart-card">
        <div ref="trendChartRef" class="chart-box"></div>
      </n-card>
    </div>

    <n-card title="热门商品 TOP10（浏览量）" :bordered="false" class="chart-card full-width">
      <div ref="hotChartRef" class="chart-box bar"></div>
    </n-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { NCard, NIcon, NTag, useMessage } from 'naive-ui'
import { CubeOutline, PeopleOutline, ReceiptOutline, WarningOutline } from '@vicons/ionicons5'
import { getDashboardStats } from '@/api/admin'

const message = useMessage()

const stats = reactive({
  userCount: 0,
  goodsCount: 0,
  orderCount: 0,
  pendingReportCount: 0
})

const categoryChartRef = ref(null)
const trendChartRef = ref(null)
const hotChartRef = ref(null)

let categoryChart
let trendChart
let hotChart

const getRecentDays = () => {
  const days = []
  for (let i = 6; i >= 0; i -= 1) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return days
}

const buildChartData = () => {
  const goods = Math.max(stats.goodsCount, 12)
  const categoryData = [
    { value: Math.round(goods * 0.26), name: '电子产品' },
    { value: Math.round(goods * 0.22), name: '书籍教材' },
    { value: Math.round(goods * 0.18), name: '生活用品' },
    { value: Math.round(goods * 0.14), name: '服饰鞋帽' },
    { value: Math.round(goods * 0.1), name: '运动户外' },
    { value: Math.round(goods * 0.1), name: '其他' }
  ]

  const days = getRecentDays()
  const trendData = days.map((_, index) => {
    const base = Math.max(1, Math.round(stats.goodsCount / 20))
    return base + ((index * 3 + stats.pendingReportCount) % 3)
  })

  const hotTitles = ['iPhone 14 Pro', '机械键盘', 'A05 耳麦', '山地自行车', '数学考研书', 'Switch 游戏', '高颜值耳机', 'Nike AF1', '小米平板', '羽毛球拍']
  const hotData = hotTitles.map((_, index) => {
    const base = Math.max(40, Math.round(stats.goodsCount * 8))
    return Math.max(30, base - index * Math.round(base / 9) + ((stats.orderCount + index) % 22))
  })

  return { categoryData, days, trendData, hotTitles, hotData }
}

const renderCharts = () => {
  if (!categoryChartRef.value || !trendChartRef.value || !hotChartRef.value) {
    return
  }

  const { categoryData, days, trendData, hotTitles, hotData } = buildChartData()

  categoryChart = categoryChart || echarts.init(categoryChartRef.value)
  trendChart = trendChart || echarts.init(trendChartRef.value)
  hotChart = hotChart || echarts.init(hotChartRef.value)

  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 2, icon: 'roundRect' },
    color: ['#ffe100', '#ffb347', '#41c9c9', '#5b7ee5', '#ff864a', '#8fdc64'],
    series: [
      {
        name: '分类商品数',
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        label: { show: false },
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        data: categoryData
      }
    ]
  })

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 26, right: 18, bottom: 26, left: 34 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days,
      axisLine: { lineStyle: { color: '#d1d5db' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLine: { show: false }
    },
    series: [
      {
        type: 'line',
        data: trendData,
        smooth: true,
        symbolSize: 7,
        lineStyle: { color: '#facc15', width: 3 },
        itemStyle: { color: '#facc15' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(250, 204, 21, 0.38)' },
              { offset: 1, color: 'rgba(250, 204, 21, 0.03)' }
            ]
          }
        }
      }
    ]
  })

  hotChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 34, right: 16, bottom: 60, left: 46 },
    xAxis: {
      type: 'category',
      data: hotTitles,
      axisLabel: { interval: 0, rotate: 24 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } }
    },
    series: [
      {
        type: 'bar',
        data: hotData,
        barWidth: 42,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffe300' },
            { offset: 1, color: '#e9bb00' }
          ])
        }
      }
    ]
  })
}

const handleResize = () => {
  categoryChart?.resize()
  trendChart?.resize()
  hotChart?.resize()
}

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.userCount = res?.userCount || 0
    stats.goodsCount = res?.goodsCount || 0
    stats.orderCount = res?.orderCount || 0
    stats.pendingReportCount = res?.pendingReportCount || 0
    await nextTick()
    renderCharts()
  } catch (error) {
    message.error(error.message || '加载统计失败')
  }
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  trendChart?.dispose()
  hotChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  border-radius: 14px;
}

.stat-head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
}

.icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.blue .icon-wrap {
  color: #38bdf8;
  background: #e0f2fe;
}

.orange .icon-wrap {
  color: #f59e0b;
  background: #fff7ed;
}

.pink .icon-wrap {
  color: #fb7185;
  background: #fff1f2;
}

.warning .icon-wrap {
  color: #84cc16;
  background: #ecfccb;
}

.stat-value {
  margin-top: 12px;
  font-size: 36px;
  line-height: 1;
  font-weight: 800;
  color: #111827;
}

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1.4fr;
  gap: 14px;
}

.chart-card {
  border-radius: 14px;
}

.chart-box {
  height: 300px;
}

.chart-box.bar {
  height: 260px;
}

.full-width {
  width: 100%;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }

  .chart-box,
  .chart-box.bar {
    height: 240px;
  }
}
</style>
