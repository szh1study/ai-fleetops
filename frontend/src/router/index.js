import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '运营总览' }
      },
      {
        path: 'fleet',
        name: 'Fleet',
        component: () => import('../views/FleetManagement.vue'),
        meta: { title: '车队管理' }
      },
      {
        path: 'incidents',
        name: 'Incidents',
        component: () => import('../views/IncidentCenter.vue'),
        meta: { title: '异常中心' }
      },
      {
        path: 'work-orders',
        name: 'WorkOrders',
        component: () => import('../views/WorkOrderCenter.vue'),
        meta: { title: '工单中心' }
      },
      {
        path: 'dispatch-forecast',
        name: 'DispatchForecast',
        component: () => import('../views/DemandForecast.vue'),
        meta: { title: '需求预测' }
      },
      {
        path: 'operation-review',
        name: 'OperationReview',
        component: () => import('../views/OperationReview.vue'),
        meta: { title: '运营复盘' }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('../views/AiAssistant.vue'),
        meta: { title: 'AI 助手' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
