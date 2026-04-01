import UserLayout from '../layout/UserLayout.vue';

export const userRoutes = [
  {
    path: '/',
    component: UserLayout,
    children: [
      { path: '', component: () => import('../views/user/home/index.vue') },
      { path: 'goods', component: () => import('../views/user/goods/list.vue') },
      { path: 'goods/:id', component: () => import('../views/user/goods/detail.vue') },
      { path: 'publish', meta: { requiresAuth: true }, component: () => import('../views/user/goods/publish.vue') },
      { path: 'publish/:id', meta: { requiresAuth: true }, component: () => import('../views/user/goods/edit.vue') },
      { path: 'me/publish', meta: { requiresAuth: true }, component: () => import('../views/user/center/my-publish.vue') },
      { path: 'me/favorite', meta: { requiresAuth: true }, component: () => import('../views/user/center/my-favorite.vue') },
      { path: 'me/order', meta: { requiresAuth: true }, component: () => import('../views/user/center/my-order.vue') },
      { path: 'me/message', meta: { requiresAuth: true }, component: () => import('../views/user/center/my-message.vue') },
      { path: 'me/profile', meta: { requiresAuth: true }, component: () => import('../views/user/center/profile.vue') },
      { path: 'me/security', meta: { requiresAuth: true }, component: () => import('../views/user/center/security.vue') }
    ]
  },
  { path: '/login', component: () => import('../views/user/auth/login.vue') },
  { path: '/register', component: () => import('../views/user/auth/register.vue') }
];
