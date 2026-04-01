import { defineStore } from 'pinia';

const TOKEN_KEY = 'campus_token';
const USER_KEY = 'campus_user';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
    role: ((JSON.parse(localStorage.getItem(USER_KEY) || 'null')?.role || '').toString().trim().toLowerCase())
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    setLogin(token, userInfo) {
      this.token = token;
      this.userInfo = userInfo;
      this.role = ((userInfo?.role || '').toString().trim().toLowerCase());
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo || {}));
    },
    logout() {
      this.token = '';
      this.userInfo = null;
      this.role = '';
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});
