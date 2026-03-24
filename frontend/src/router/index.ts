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
      path: '/goals',
      name: 'goals',
      component: () => import('../views/goals/index.vue'),
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
    {
      path: '/insights/report',
      name: 'insight-report',
      component: () => import('../views/insights/report.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/insights/mood',
      name: 'insight-mood',
      component: () => import('../views/insights/mood.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/insights/skills',
      name: 'insight-skills',
      component: () => import('../views/insights/skills.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/insights/review-alerts',
      name: 'insight-review-alerts',
      component: () => import('../views/insights/review-alerts.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN'] },
    },
    {
      path: '/insights/risk-radar',
      name: 'insight-risk-radar',
      component: () => import('../views/insights/risk-radar.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
    {
      path: '/insights/retrospective',
      name: 'insight-retrospective',
      component: () => import('../views/insights/retrospective.vue'),
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
    },
  ],
})

export default router
