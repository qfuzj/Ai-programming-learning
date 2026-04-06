import request from '../utils/request'

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

export function adminUserPage(params) {
  return request.get('/admin/user/page', { params })
}

export function updateAdminUserStatus(id, status) {
  return request.put(`/admin/user/status/${id}`, null, { params: { status } })
}

export function adminGoodsPage(params) {
  return request.get('/admin/goods/page', { params })
}

export function adminGoodsDetail(id) {
  return request.get(`/admin/goods/detail/${id}`)
}

export function updateAdminGoodsStatus(id, data) {
  return request.put(`/admin/goods/status/${id}`, data)
}

export function deleteAdminGoods(id) {
  return request.delete(`/admin/goods/delete/${id}`)
}

export function adminCategoryList() {
  return request.get('/admin/category/list')
}

export function addAdminCategory(data) {
  return request.post('/admin/category/add', data)
}

export function updateAdminCategory(data) {
  return request.put('/admin/category/update', data)
}

export function updateAdminCategoryStatus(id, status) {
  return request.put(`/admin/category/status/${id}`, null, { params: { status } })
}

export function deleteAdminCategory(id) {
  return request.delete(`/admin/category/delete/${id}`)
}

export function adminOrderPage(params) {
  return request.get('/admin/order/page', { params })
}

export function closeAdminOrder(id) {
  return request.put(`/admin/order/close/${id}`)
}

export function adminNoticePage(params) {
  return request.get('/admin/notice/page', { params })
}

export function addAdminNotice(data) {
  return request.post('/admin/notice/add', data)
}

export function updateAdminNotice(data) {
  return request.put('/admin/notice/update', data)
}

export function updateAdminNoticeStatus(id, status) {
  return request.put(`/admin/notice/status/${id}`, null, { params: { status } })
}

export function deleteAdminNotice(id) {
  return request.delete(`/admin/notice/delete/${id}`)
}

export function adminBannerPage(params) {
  return request.get('/admin/banner/page', { params })
}

export function addAdminBanner(data) {
  return request.post('/admin/banner/add', data)
}

export function updateAdminBanner(data) {
  return request.put('/admin/banner/update', data)
}

export function updateAdminBannerStatus(id, status) {
  return request.put(`/admin/banner/status/${id}`, null, { params: { status } })
}

export function deleteAdminBanner(id) {
  return request.delete(`/admin/banner/delete/${id}`)
}

export function adminReportPage(params) {
  return request.get('/admin/report/page', { params })
}

export function reviewAdminReport(id, data) {
  return request.put(`/admin/report/review/${id}`, data)
}

export function adminCommentPage(params) {
  return request.get('/admin/comment/page', { params })
}

export function deleteAdminComment(id) {
  return request.delete(`/admin/comment/delete/${id}`)
}
