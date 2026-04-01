import request from '../utils/request'

export function listGoods(params) {
  return request.get('/goods/list', { params })
}

export function getGoodsDetail(id) {
  return request.get(`/goods/detail/${id}`)
}

export function addGoods(data) {
  return request.post('/goods/add', data)
}

export function updateGoods(data) {
  return request.put('/goods/update', data)
}

export function deleteGoods(id) {
  return request.delete(`/goods/delete/${id}`)
}

export function myGoodsList(params) {
  return request.get('/goods/my/list', { params })
}

export function putawayGoods(id) {
  return request.put(`/goods/putaway/${id}`)
}

export function soldoutGoods(id) {
  return request.put(`/goods/soldout/${id}`)
}

export function uploadGoodsImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
