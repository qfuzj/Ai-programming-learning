/**
 * 应用入口：初始化 Vue、Pinia、Router 与全局样式。
 */
import { createApp } from "vue";
import App from "./App.vue";
import { setupStore } from "@/store";
import { setupRouter } from "@/router";
import "./styles.css";

// 修复按需引入时，编程式 API（如 ElMessage）不显示样式的问题
import "element-plus/theme-chalk/src/message.scss";
import "element-plus/theme-chalk/src/message-box.scss";
import "element-plus/theme-chalk/src/notification.scss";
import "element-plus/theme-chalk/src/loading.scss";

async function bootstrap() {
  const app = createApp(App);
  setupStore(app);
  await setupRouter(app);
  app.mount("#app");
}

void bootstrap();
