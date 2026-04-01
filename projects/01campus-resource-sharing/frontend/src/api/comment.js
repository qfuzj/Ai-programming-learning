import request from '../utils/request'

export function addComment(data) {
  return request.post('/comment/add', data)
}

export function listCommentByGoods(goodsId) {
  return request.get(`/comment/listByGoods/${goodsId}`)
}
