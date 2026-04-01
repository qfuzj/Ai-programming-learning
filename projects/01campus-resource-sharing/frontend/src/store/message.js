import { defineStore } from 'pinia';

export const useMessageStore = defineStore('message', {
  state: () => ({
    unreadCount: 0,
    noticeCount: 0
  }),
  actions: {
    setUnreadCount(value) {
      this.unreadCount = value;
    },
    setNoticeCount(value) {
      this.noticeCount = value;
    }
  }
});
