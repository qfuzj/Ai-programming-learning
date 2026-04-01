<template>
  <div class="my-publish-page">
    <n-card title="我的发布" :bordered="false">
      <div class="toolbar">
        <n-input v-model:value="query.keyword" placeholder="按标题搜索" clearable @keyup.enter="loadList" />
        <n-button type="primary" @click="loadList">查询</n-button>
        <n-button @click="goPublish">发布新商品</n-button>
      </div>

      <div v-if="page.records.length" class="rows">
        <div class="row" v-for="item in page.records" :key="item.id">
          <img class="thumb" :src="resolveFileUrl(item.mainImage)" alt="thumb" />
          <div class="info">
            <div class="title">{{ item.title }}</div>
            <div class="meta">价格：¥{{ item.price }} | 状态：{{ item.status }}</div>
          </div>
          <div class="ops">
            <n-button size="small" @click="goEdit(item.id)">编辑</n-button>
            <n-button size="small" @click="toggleStatus(item)">{{ item.status === 'on_sale' ? '下架' : '上架' }}</n-button>
            <n-button size="small" type="error" @click="remove(item.id)">删除</n-button>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无发布商品" />

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
import { NButton, NCard, NEmpty, NInput, NPagination, useDialog, useMessage } from 'naive-ui'
import { deleteGoods, myGoodsList, putawayGoods, soldoutGoods } from '@/api/goods'
import { resolveFileUrl } from '@/utils/file'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const page = reactive({ total: 0, records: [] })

const loadList = async () => {
  try {
    const res = await myGoodsList(query)
    page.total = res?.total || 0
    page.records = res?.records || []
  } catch (error) {
    message.error(error.message || '加载失败')
  }
}

const goEdit = (id) => router.push(`/publish/${id}`)
const goPublish = () => router.push('/publish')

const toggleStatus = async (item) => {
  try {
    if (item.status === 'on_sale') {
      await soldoutGoods(item.id)
      message.success('下架成功')
    } else {
      await putawayGoods(item.id)
      message.success('上架成功')
    }
    loadList()
  } catch (error) {
    message.error(error.message || '操作失败')
  }
}

const remove = (id) => {
  dialog.warning({
    title: '确认删除',
    content: '删除后不可恢复，确定删除吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteGoods(id)
      message.success('删除成功')
      loadList()
    }
  })
}

onMounted(loadList)
</script>

<style scoped>
.my-publish-page { max-width: 1000px; margin: 0 auto; }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.rows { display: flex; flex-direction: column; gap: 10px; }
.row { display: grid; grid-template-columns: 90px 1fr auto; gap: 12px; align-items: center; border: 1px solid #eee; border-radius: 8px; padding: 10px; }
.thumb { width: 90px; height: 90px; object-fit: cover; border-radius: 6px; }
.title { font-weight: 600; margin-bottom: 6px; }
.meta { color: #666; font-size: 13px; }
.ops { display: flex; gap: 8px; }
.pager { margin-top: 14px; display: flex; justify-content: flex-end; }
</style>
