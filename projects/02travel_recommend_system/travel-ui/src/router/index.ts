/**
 * 路由主入口：注册用户端/管理端路由并挂载鉴权守卫。
 */
import type { App } from "vue";
import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { userRoutes } from "@/router/routes/user";
import { adminRoutes } from "@/router/routes/admin";
import { setupAuthGuard } from "@/router/guards/auth";

const baseRoutes: RouteRecordRaw[] = [
  ...userRoutes,
  ...adminRoutes,
  {
    path: "/403",
    name: "Forbidden",
    component: () => import("@/views/error/ForbiddenView.vue"),
    meta: { title: "无权限", requiresAuth: false, hidden: true },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/error/NotFoundView.vue"),
    meta: { title: "页面不存在", requiresAuth: false, hidden: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes: baseRoutes,
  scrollBehavior: () => ({ top: 0, left: 0 }),
});

export async function setupRouter(app: App<Element>): Promise<void> {
  setupAuthGuard(router);
  app.use(router);
  await router.isReady();
}

export default router;
