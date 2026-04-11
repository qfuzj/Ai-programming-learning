/**
 * 管理端路由：全部挂载在 AdminLayout 下。
 */
import type { RouteRecordRaw } from "vue-router";
import AdminLayout from "@/layouts/AdminLayout.vue";

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: "/admin",
    component: AdminLayout,
    redirect: "/admin/dashboard",
    meta: { title: "管理端", requiresAuth: true, roles: ["ADMIN"] },
    children: [
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: () => import("@/views/admin/DashboardView.vue"),
        meta: { title: "工作台" },
      },
      {
        path: "scenic",
        name: "AdminScenic",
        component: () => import("@/views/admin/ScenicManageView.vue"),
        meta: { title: "景点管理" },
      },
      {
        path: "review",
        name: "AdminReview",
        component: () => import("@/views/admin/ReviewManageView.vue"),
        meta: { title: "评论审核" },
      },
      {
        path: "user",
        name: "AdminUser",
        component: () => import("@/views/admin/UserManageView.vue"),
        meta: { title: "用户管理" },
      },
      {
        path: "regions",
        name: "AdminRegions",
        component: () => import("@/views/admin/RegionManageView.vue"),
        meta: { title: "地区管理" },
      },
      {
        path: "tags",
        name: "AdminTags",
        component: () => import("@/views/admin/TagManageView.vue"),
        meta: { title: "标签管理" },
      },
      {
        path: "system-config",
        name: "AdminSystemConfig",
        component: () => import("@/views/admin/SystemConfigView.vue"),
        meta: { title: "系统配置" },
      },
      {
        path: "logs",
        name: "AdminLogs",
        component: () => import("@/views/admin/LogView.vue"),
        meta: { title: "操作日志" },
      },
      {
        path: "analytics/recommend",
        name: "AdminRecommendAnalytics",
        component: () => import("@/views/admin/RecommendAnalyticsView.vue"),
        meta: { title: "推荐分析" },
      },
      {
        path: "analytics/llm",
        name: "AdminLlmAnalytics",
        component: () => import("@/views/admin/LlmAnalyticsView.vue"),
        meta: { title: "LLM 分析" },
      },
    ],
  },
  {
    path: "/admin/login",
    name: "AdminLogin",
    component: () => import("@/views/auth/admin-login.vue"),
    meta: { title: "后台登录", requiresAuth: false, hidden: true },
  },
];
