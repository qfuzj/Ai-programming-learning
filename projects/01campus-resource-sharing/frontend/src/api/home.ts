import request from '@/utils/request'

// 获取轮播图
export function getBanners() {
  return request.get('/home/banners')
}

// 获取推荐商品
export function getRecommend() {
  return request.get('/home/recommend')
}

// 获取最新商品
export function getLatest() {
  return request.get('/home/latest')
}

// 获取公告
export function getNotices() {
  return request.get('/home/notices')
}

// 获取分类列表
export function getCategories() {
  return request.get('/category/list')
}
