import { createRouter, createWebHistory } from 'vue-router'
import type { UserRole } from '../types/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: UserRole[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/index.vue'),
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/dashboard/index.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/logs',
      name: 'logs',
      component: () => import('../views/logs/index.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/reviews',
      name: 'reviews',
      component: () => import('../views/reviews/index.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/admin/index.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN'] },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/profile/index.vue'),
      meta: { requiresAuth: true, roles: ['EMPLOYEE'] },
    },
    {
      path: '/password',
      name: 'password',
      component: () => import('../views/password/index.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
  ],
})

export default router
