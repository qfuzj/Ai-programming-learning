import request from '../utils/request'

export function ping() {
  return request.get('/common/ping')
}
