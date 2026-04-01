import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    loading: false,
    themePrimary: '#ffe60f'
  }),
  actions: {
    setLoading(value) {
      this.loading = value;
    }
  }
});
