/**
 * Store 入口：注册 Pinia 并导出模块。
 */
import type { App } from "vue";
import { createPinia } from "pinia";

const pinia = createPinia();

export function setupStore(app: App<Element>): void {
  app.use(pinia);
}

export { pinia };
export { useUserStore } from "@/store/modules/user";
export { useAuthStore } from "@/store/modules/auth";
export { useAppStore } from "@/store/modules/app";
