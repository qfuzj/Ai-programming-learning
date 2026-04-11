/**
 * 路由扩展元信息：用于角色限制和导航展示。
 */
import type { UserRole } from "@/types/auth";

export interface RouteMetaAuth {
  title: string;
  // 可选：仅当值为 true 时才表示必须登录；false/undefined 视为公开路由。
  requiresAuth?: boolean;
  roles?: UserRole[];
  hidden?: boolean;
}

declare module "vue-router" {
  interface RouteMeta extends RouteMetaAuth {}
}
