import { defineStore } from 'pinia'

const TOKEN_KEY = 'campus_token'
const USER_KEY = 'campus_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'admin'
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo || {}))
    },
    setLogin(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo || {}))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
