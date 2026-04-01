import request from '../utils/request'

export function addFavorite(goodsId) {
  return request.post('/favorite/add', { goodsId })
}

export function removeFavorite(goodsId) {
  return request.delete(`/favorite/remove/${goodsId}`)
}

export function listFavorites(params) {
  return request.get('/favorite/list', { params })
}

export function getFavoriteStatus(goodsId) {
  return request.get(`/favorite/status/${goodsId}`)
}
