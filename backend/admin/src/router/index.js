import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '管理员入驻申请' }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/ResetPassword.vue'),
    meta: { title: '重置密码' }
  },
  {
    path: '/',
    component: () => import('../views/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/users/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'travel',
        name: 'Travel',
        component: () => import('../views/travel/Travel.vue'),
        meta: { title: '出行审核' }
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('../views/shop/Shop.vue'),
        meta: { title: '商城管理' }
      },
      {
        path: 'activity',
        name: 'Activity',
        component: () => import('../views/activity/Activity.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('../views/forum/Forum.vue'),
        meta: { title: '论坛管理' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('../views/points/Points.vue'),
        meta: { title: '积分规则' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('../views/announcement/Announcement.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/settings/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('../views/notifications/Notifications.vue'),
        meta: { title: '通知中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 低碳管理系统` : '低碳管理系统'
  
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    
    if (!token) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    if (userRole !== 'admin') {
      ElMessage.error('非管理员账号无权访问后台')
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      next('/login')
      return
    }
  }
  
  next()
})

export default router
