import request from '../utils/request'

export function createOrder(data) {
  return request.post('/order/create', data)
}

export function buyerOrderList(params) {
  return request.get('/order/buyer/list', { params })
}

export function sellerOrderList(params) {
  return request.get('/order/seller/list', { params })
}

export function getOrderDetail(id) {
  return request.get(`/order/detail/${id}`)
}

export function cancelOrder(id) {
  return request.put(`/order/cancel/${id}`)
}

export function confirmOrder(id) {
  return request.put(`/order/confirm/${id}`)
}

export function completeOrder(id) {
  return request.put(`/order/complete/${id}`)
}
