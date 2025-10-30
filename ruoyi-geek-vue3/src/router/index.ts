import { createWebHistory, createRouter, createWebHashHistory } from 'vue-router'
import { constantRoutes } from './routes/staticRoutes'
import { configureNProgress } from './utils/utils'
import { setupBeforeEachGuard } from './guards/beforeEach'
import { setupAfterEachGuard } from './guards/afterEach'
import type { App } from 'vue'

export const router = createRouter({
  // createWebHistory  createWebHashHistory
  history: createWebHistory(import.meta.env.VITE_BASE_ROUTER),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
});

// 初始化路由
export default function initRouter(app: App<Element>): void {
  configureNProgress() // 顶部进度条
  setupBeforeEachGuard(router) // 路由前置守卫
  setupAfterEachGuard(router) // 路由后置守卫
  app.use(router)
}
