/**
 * 应用入口：初始化 Vue、Pinia、Router 与全局样式。
 */
import { createApp } from "vue";
import App from "./App.vue";
import { setupStore } from "@/store";
import { setupRouter } from "@/router";
import "./styles.css";

async function bootstrap() {
  const app = createApp(App);
  setupStore(app);
  await setupRouter(app);
  app.mount("#app");
}

void bootstrap();
