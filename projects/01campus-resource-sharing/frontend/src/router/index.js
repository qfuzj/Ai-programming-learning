import { createRouter, createWebHistory } from 'vue-router';
import { userRoutes } from './user-routes';
import { adminRoutes } from './admin-routes';
import { useUserStore } from '../store/user';

const routes = [
  ...userRoutes,
  ...adminRoutes
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const userStore = useUserStore();
  const normalizedRole = (userStore.role || '').toString().trim().toLowerCase();

  if (to.path === '/admin/login' && userStore.isLogin && normalizedRole === 'admin') {
    return '/admin/dashboard';
  }

  if (!to.meta.requiresAuth) {
    return true;
  }

  if (!userStore.isLogin) {
    if (to.path.startsWith('/admin')) {
      return '/admin/login';
    }
    return '/login';
  }

  if (to.meta.role && to.meta.role !== normalizedRole) {
    if (to.path.startsWith('/admin')) {
      return '/admin/login';
    }
    return '/';
  }

  return true;
});

export default router;
