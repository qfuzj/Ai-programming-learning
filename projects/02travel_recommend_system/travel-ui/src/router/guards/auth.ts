/**
 * 路由守卫：登录校验、角色鉴权、登录页重定向。
 */
import type { Router } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";

function getLoginPath(targetPath: string): string {
  return targetPath.startsWith("/admin") ? ROUTE_PATHS.ADMIN_LOGIN : ROUTE_PATHS.USER_LOGIN;
}

/**
 * 检查 JWT Token 是否过期
 * @param token JWT Token 字符串
 * @returns true 表示已过期或无效，false 表示有效
 */
function isTokenExpired(token: string): boolean {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return true;
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const payload = JSON.parse(atob(base64));
    if (!payload.exp) return false; // 无过期时间，认为有效
    return payload.exp * 1000 < Date.now();
  } catch {
    return true; // 解析失败视为过期
  }
}

export function setupAuthGuard(router: Router): void {
  router.beforeEach((to) => {
    const userStore = useUserStore();
    const isAuthed = userStore.isAuthenticated;
    const role = userStore.role;
    // requiresAuth 为可选字段，只有显式 true 才认定为受保护路由。
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth === true);
    const requiredRoles = Array.from(
      new Set(to.matched.flatMap((record) => (record.meta.roles ?? []) as string[]))
    );

    if (to.path === ROUTE_PATHS.USER_LOGIN) {
      if (isAuthed) {
        // ADMIN 不应进入用户端登录页，已登录时统一回到管理端工作台。
        return role === "ADMIN"
          ? { path: ROUTE_PATHS.ADMIN_DASHBOARD }
          : { path: ROUTE_PATHS.USER_HOME };
      }
      return true;
    }

    if (to.path === ROUTE_PATHS.ADMIN_LOGIN) {
      if (isAuthed) {
        return role === "ADMIN"
          ? { path: ROUTE_PATHS.ADMIN_DASHBOARD }
          : { path: ROUTE_PATHS.FORBIDDEN };
      }
      return true;
    }

    if (!requiresAuth) {
      return true;
    }

    if (!isAuthed || isTokenExpired(userStore.token || "")) {
      if (isAuthed && isTokenExpired(userStore.token || "")) {
        // Token 过期，清除认证信息
        userStore.clearAuth();
      }
      const loginPath = getLoginPath(to.path);
      return { path: loginPath, query: { redirect: to.fullPath } };
    }

    if (requiredRoles.length > 0 && (!role || !requiredRoles.includes(role))) {
      return { path: ROUTE_PATHS.FORBIDDEN };
    }

    return true;
  });
}
