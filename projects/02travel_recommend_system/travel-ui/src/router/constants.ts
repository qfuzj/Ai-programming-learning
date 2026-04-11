/**
 * 路由路径常量：避免魔法字符串分散在守卫与登录流程中。
 */
export const ROUTE_PATHS = {
  USER_HOME: "/",
  USER_LOGIN: "/login",
  ADMIN_LOGIN: "/admin/login",
  ADMIN_DASHBOARD: "/admin/dashboard",
  FORBIDDEN: "/403",
} as const;
