/**
 * 全局状态：维护全局加载态与应用标题。
 */
import { computed, ref } from "vue";
import { defineStore } from "pinia";

export const useAppStore = defineStore("app", () => {
  const appName = ref("LLM-Travel-Advisor");
  const loading = ref(false);

  const loadingText = computed(() => (loading.value ? "加载中..." : ""));

  function setLoading(value: boolean): void {
    loading.value = value;
  }

  return {
    appName,
    loading,
    loadingText,
    setLoading,
  };
});
