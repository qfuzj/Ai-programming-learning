/**
 * 用户端路由：全部挂载在 UserLayout 下。
 */
import type { RouteRecordRaw } from "vue-router";
import UserLayout from "@/layouts/UserLayout.vue";

export const userRoutes: RouteRecordRaw[] = [
  {
    path: "/",
    component: UserLayout,
    meta: { title: "用户端", requiresAuth: false },
    children: [
      {
        path: "",
        name: "UserHome",
        component: () => import("@/views/user/HomeView.vue"),
        meta: { title: "首页", requiresAuth: false },
      },
      {
        path: "scenic",
        name: "ScenicList",
        component: () => import("@/views/user/ScenicListView.vue"),
        meta: { title: "景点列表", requiresAuth: false },
      },
      {
        path: "scenic/:id",
        name: "ScenicDetail",
        component: () => import("@/views/user/ScenicDetailView.vue"),
        meta: { title: "景点详情", requiresAuth: false },
      },
      {
        path: "favorites",
        name: "Favorites",
        component: () => import("@/views/user/FavoritesView.vue"),
        meta: { title: "收藏页", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "history",
        name: "BrowseHistory",
        component: () => import("@/views/user/HistoryView.vue"),
        meta: { title: "浏览历史", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "my-reviews",
        name: "MyReviews",
        component: () => import("@/views/user/MyReviewsView.vue"),
        meta: { title: "我的点评", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "ai/recommend",
        name: "AiRecommend",
        component: () => import("@/views/user/AiRecommendView.vue"),
        meta: { title: "AI 推荐", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "ai/chat",
        name: "AiChat",
        component: () => import("@/views/user/ChatListView.vue"),
        meta: { title: "AI 聊天", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "ai/chat/:conversationId",
        name: "AiChatDetail",
        component: () => import("@/views/user/ChatDetailView.vue"),
        meta: { title: "AI 聊天详情", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "itinerary",
        name: "ItineraryList",
        component: () => import("@/views/user/ItineraryListView.vue"),
        meta: { title: "行程列表", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "itinerary/create",
        name: "ItineraryCreate",
        component: () => import("@/views/user/ItineraryCreateView.vue"),
        meta: { title: "新建行程", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "itinerary/ai-generate",
        name: "ItineraryAiGenerate",
        component: () => import("@/views/user/ItineraryAiGenerateView.vue"),
        meta: { title: "AI 生成行程", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "itinerary/:id",
        name: "ItineraryDetail",
        component: () => import("@/views/user/ItineraryDetailView.vue"),
        meta: { title: "行程详情", requiresAuth: true, roles: ["USER"] },
      },
      {
        path: "profile",
        name: "UserProfile",
        component: () => import("@/views/user/ProfileView.vue"),
        meta: { title: "个人中心", requiresAuth: true, roles: ["USER"] },
      },
    ],
  },
  {
    path: "/login",
    name: "UserLogin",
    component: () => import("@/views/auth/login.vue"),
    meta: { title: "用户登录", requiresAuth: false, hidden: true },
  },
  {
    path: "/register",
    name: "UserRegister",
    component: () => import("@/views/auth/register.vue"),
    meta: { title: "用户注册", requiresAuth: false, hidden: true },
  },
  {
    path: "/reset-password",
    name: "UserResetPassword",
    component: () => import("@/views/auth/reset-password.vue"),
    meta: { title: "重置密码", requiresAuth: false, hidden: true },
  },
];
