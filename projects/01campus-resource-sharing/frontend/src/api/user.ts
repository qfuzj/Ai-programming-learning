import request from '@/utils/request'

// 获取个人资料
export function getProfile() {
  return request.get('/user/profile')
}

// 修改个人资料
export function updateProfile(data) {
  return request.put('/user/update', data)
}

// 修改密码
export function changePassword(data) {
  return request.put('/user/password', data)
}

// 上传头像
export function uploadAvatar(data) {
  return request.post('/user/avatar', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
