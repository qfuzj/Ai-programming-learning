import request from '../utils/request'

export function sendMessage(data) {
  return request.post('/message/send', data)
}

export function listMessages(params) {
  return request.get('/message/list', { params })
}

export function readMessage(id) {
  return request.put(`/message/read/${id}`)
}
