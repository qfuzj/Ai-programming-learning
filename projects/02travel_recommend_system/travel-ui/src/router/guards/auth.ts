/**
 * 路由守卫：登录校验、角色鉴权、登录页重定向。
 */
import type { Router } from "vue-router";
import { useUserStore } from "@/store";
import { ROUTE_PATHS } from "@/router/constants";

function getLoginPath(targetPath: string): string {
  return targetPath.startsWith("/admin") ? ROUTE_PATHS.ADMIN_LOGIN : ROUTE_PATHS.USER_LOGIN;
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

    if (!isAuthed) {
      const loginPath = getLoginPath(to.path);
      return { path: loginPath, query: { redirect: to.fullPath } };
    }

    if (requiredRoles.length > 0 && (!role || !requiredRoles.includes(role))) {
      return { path: ROUTE_PATHS.FORBIDDEN };
    }

    return true;
  });
}
