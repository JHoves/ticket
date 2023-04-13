import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    //懒加载
    component: () => import('../views/login.vue')
  },
  {
    path: '/',
    name: 'main',
    //懒加载
    component: () => import('../views/main.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
