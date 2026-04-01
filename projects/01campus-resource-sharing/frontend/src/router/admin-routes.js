import AdminLayout from '../layout/AdminLayout.vue';

export const adminRoutes = [
  { path: '/admin/login', component: () => import('../views/admin/login.vue') },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/dashboard/index.vue') },
      { path: 'user', component: () => import('../views/admin/user/index.vue') },
      { path: 'goods', component: () => import('../views/admin/goods/index.vue') },
      { path: 'category', component: () => import('../views/admin/category/index.vue') },
      { path: 'order', component: () => import('../views/admin/order/index.vue') },
      { path: 'notice', component: () => import('../views/admin/notice/index.vue') },
      { path: 'banner', component: () => import('../views/admin/banner/index.vue') },
      { path: 'report', component: () => import('../views/admin/report/index.vue') },
      { path: 'comment', component: () => import('../views/admin/comment/index.vue') }
    ]
  }
];
