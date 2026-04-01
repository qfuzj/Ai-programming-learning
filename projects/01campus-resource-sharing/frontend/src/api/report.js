import request from '../utils/request'

export function addReport(data) {
  return request.post('/report/add', data)
}
