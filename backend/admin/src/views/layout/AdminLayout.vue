<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <span v-if="!isCollapsed" class="logo-text">低碳出行管理</span>
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <router-link to="/dashboard" class="nav-item" :class="{ active: isActive('/dashboard') }">
          <el-icon :size="20"><Odometer /></el-icon>
          <span v-if="!isCollapsed">仪表盘</span>
        </router-link>
        <router-link to="/users" class="nav-item" :class="{ active: isActive('/users') }">
          <el-icon :size="20"><User /></el-icon>
          <span v-if="!isCollapsed">用户管理</span>
        </router-link>
        <router-link to="/travel" class="nav-item" :class="{ active: isActive('/travel') }">
          <el-icon :size="20"><Location /></el-icon>
          <span v-if="!isCollapsed">出行审核</span>
        </router-link>
        <router-link to="/shop" class="nav-item" :class="{ active: isActive('/shop') }">
          <el-icon :size="20"><ShoppingCart /></el-icon>
          <span v-if="!isCollapsed">商城管理</span>
        </router-link>
        <router-link to="/activity" class="nav-item" :class="{ active: isActive('/activity') }">
          <el-icon :size="20"><Calendar /></el-icon>
          <span v-if="!isCollapsed">活动管理</span>
        </router-link>
        <router-link to="/forum" class="nav-item" :class="{ active: isActive('/forum') }">
          <el-icon :size="20"><ChatDotRound /></el-icon>
          <span v-if="!isCollapsed">论坛管理</span>
        </router-link>
        <router-link to="/points" class="nav-item" :class="{ active: isActive('/points') }">
          <el-icon :size="20"><Star /></el-icon>
          <span v-if="!isCollapsed">积分规则</span>
        </router-link>
        <router-link to="/announcement" class="nav-item" :class="{ active: isActive('/announcement') }">
          <el-icon :size="20"><Bell /></el-icon>
          <span v-if="!isCollapsed">公告管理</span>
        </router-link>
        <router-link to="/notifications" class="nav-item" :class="{ active: isActive('/notifications') }">
          <el-icon :size="20"><Message /></el-icon>
          <span v-if="!isCollapsed">通知中心</span>
          <span v-if="!isCollapsed && notificationCount > 0" class="badge">{{ notificationCount }}</span>
        </router-link>
        
        <div class="nav-divider"></div>
        
        <router-link to="/settings" class="nav-item" :class="{ active: isActive('/settings') }">
          <el-icon :size="20"><Setting /></el-icon>
          <span v-if="!isCollapsed">系统设置</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info" v-if="!isCollapsed">
          <div class="user-avatar">
            <el-icon :size="20"><UserFilled /></el-icon>
          </div>
          <div class="user-details">
            <span class="user-name">管理员</span>
            <span class="user-role">超级管理员</span>
          </div>
          <el-dropdown trigger="click" @command="handleCommand">
            <el-icon class="logout-icon"><SwitchButton /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </aside>

    <div class="main-wrapper">
      <header class="header">
        <div class="header-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon :size="20"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户/帖子..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          <el-badge :value="notificationCount" :hidden="notificationCount === 0" :max="99">
            <el-dropdown trigger="click" @visible-change="handleNotificationDropdown">
              <button class="icon-btn">
                <el-icon :size="20"><Bell /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu class="notification-dropdown">
                  <div class="notification-header">
                    <span>通知消息</span>
                    <div class="header-actions">
                      <el-button class="btn-orange-text" text size="small" @click="markAllRead" v-if="notificationCount > 0">全部已读</el-button>
                      <el-button class="btn-orange-text" text size="small" @click="router.push('/notifications')">查看全部</el-button>
                    </div>
                  </div>
                  <div class="notification-list" v-if="notifications.length > 0">
                    <div 
                      v-for="item in notifications" 
                      :key="item.id" 
                      class="notification-item"
                      :class="{ unread: !item.isRead }"
                      @click="handleNotificationClick(item)"
                    >
                      <div class="notification-content">
                        <div class="notification-title">{{ item.title || '系统通知' }}</div>
                        <div class="notification-desc">{{ item.content?.slice(0, 50) || '' }}</div>
                        <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
                      </div>
                    </div>
                  </div>
                  <div class="notification-empty" v-else>
                    暂无通知
                  </div>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-badge>
          <el-dropdown trigger="click">
            <button class="icon-btn">
              <el-icon :size="20"><Setting /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/settings')">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="router.push('/dashboard')">
                  <el-icon><Odometer /></el-icon>
                  返回首页
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Odometer, User, Location, ShoppingCart, Calendar,
  ChatDotRound, Star, Bell, Setting, UserFilled, SwitchButton,
  Fold, Expand, Search, Message
} from '@element-plus/icons-vue'
import api from '../../api'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const searchQuery = ref('')
const notificationCount = ref(0)
const notifications = ref([])

const currentRoute = computed(() => route)

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/settings')
  }
}

const handleSearch = () => {
  if (!searchQuery.value.trim()) return
  ElMessage.info(`搜索: ${searchQuery.value}`)
}

const loadNotificationCount = async () => {
  try {
    const res = await api.get('/messages/unread-count')
    notificationCount.value = res || 0
  } catch (e) {
    console.error('Failed to load notification count:', e)
  }
}

const loadNotifications = async () => {
  try {
    const res = await api.get('/messages', { params: { page: 1, size: 10 } })
    notifications.value = res.records || []
  } catch (e) {
    console.error('Failed to load notifications:', e)
  }
}

const handleNotificationDropdown = (visible) => {
  if (visible) {
    loadNotifications()
  }
}

const markAllRead = async () => {
  try {
    await api.put('/messages/read-all')
    notificationCount.value = 0
    notifications.value = notifications.value.map(n => ({ ...n, isRead: 1 }))
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleNotificationClick = async (item) => {
  if (!item.isRead) {
    try {
      await api.put(`/messages/${item.id}/read`)
      item.isRead = 1
      notificationCount.value = Math.max(0, notificationCount.value - 1)
    } catch (e) {
      console.error('Failed to mark as read:', e)
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.slice(0, 10)
}

onMounted(() => {
  loadNotificationCount()
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-light);
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.nav-item:hover {
  background: var(--primary-bg);
  color: var(--primary-color);
}

.nav-item.active {
  background: var(--primary-bg);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-item .el-icon {
  flex-shrink: 0;
}

.nav-item .badge {
  margin-left: auto;
  background: #f97316;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.nav-divider {
  height: 1px;
  background: var(--border-color);
  margin: 16px 0;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-lg);
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: var(--primary-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
}

.logout-icon {
  cursor: pointer;
  color: var(--text-muted);
  transition: color 0.2s;
}

.logout-icon:hover {
  color: var(--danger-color);
}

.main-wrapper {
  flex: 1;
  margin-left: 260px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  min-width: 0;
}

.sidebar.collapsed + .main-wrapper {
  margin-left: 72px;
}

.header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--radius);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: #f3f4f6;
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  width: 240px;
}

.search-box :deep(.el-input__wrapper) {
  background: #f3f4f6;
  border: none;
  border-radius: var(--radius-lg);
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--radius);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f3f4f6;
  color: var(--primary-color);
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  height: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.notification-dropdown {
  width: 320px;
  max-height: 400px;
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-primary);
}

.notification-header .header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f3f4f6;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: #fff7ed;
}

.notification-item.unread:hover {
  background: #fff3e6;
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.notification-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: var(--text-muted);
}

.notification-empty {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
}

/* 通知下拉按钮 - 橙色文字按钮 */
:deep(.btn-orange-text) {
  color: #ec5b13 !important;
  font-weight: 500;
}
:deep(.btn-orange-text:hover) {
  color: #d14d0b !important;
  background: #fff7ed !important;
}
</style>
