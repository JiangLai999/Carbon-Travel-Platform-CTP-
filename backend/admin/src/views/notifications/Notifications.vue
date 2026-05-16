<template>
  <div class="notifications-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">通知中心</h1>
        <p class="page-subtitle">管理系统通知和消息</p>
      </div>
      <div class="header-actions">
        <el-button @click="markAllRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
        <el-button type="info" @click="deleteAllRead" :disabled="readCount === 0">
          <el-icon><Delete /></el-icon>
          清空已读
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="24"><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalCount }}</span>
          <span class="stat-label">全部通知</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="24"><Message /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ unreadCount }}</span>
          <span class="stat-label">未读通知</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ readCount }}</span>
          <span class="stat-label">已读通知</span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterType" @change="loadNotifications">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="unread">未读</el-radio-button>
        <el-radio-button value="read">已读</el-radio-button>
      </el-radio-group>
      <el-select v-model="notificationType" placeholder="通知类型" clearable @change="loadNotifications" style="width: 150px; margin-left: 16px;">
        <el-option label="全部类型" value="" />
        <el-option label="出行审核" value="travel" />
        <el-option label="活动参与" value="activity" />
        <el-option label="兑换订单" value="exchange" />
        <el-option label="系统通知" value="system" />
      </el-select>
    </div>

    <div class="notification-list" v-loading="loading">
      <div 
        class="notification-item" 
        v-for="item in notifications" 
        :key="item.id"
        :class="{ unread: !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="notification-icon" :class="getTypeClass(item.type)">
          <el-icon :size="20">
            <component :is="getTypeIcon(item.type)" />
          </el-icon>
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-title">{{ item.title || '系统通知' }}</span>
            <span class="notification-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <p class="notification-desc">{{ item.content }}</p>
          <div class="notification-tags">
            <el-tag size="small" :type="getTagType(item.type)">{{ getTypeName(item.type) }}</el-tag>
            <el-tag size="small" :type="item.isRead ? 'info' : 'warning'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
          </div>
        </div>
        <div class="notification-actions">
        <el-button 
          v-if="!item.isRead" 
          class="btn-indigo-light" 
          text 
          size="small" 
          @click.stop="markAsRead(item)"
        >
          标记已读
        </el-button>
        <el-button 
          class="btn-gray-light" 
          text 
          size="small" 
          @click.stop="deleteNotification(item)"
        >
          删除
        </el-button>
        </div>
      </div>

      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
    </div>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadNotifications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Bell, Check, Delete, Message, CircleCheck, 
  Van, Calendar, ShoppingCart, Setting 
} from '@element-plus/icons-vue'
import api from '../../api'

const loading = ref(false)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filterType = ref('all')
const notificationType = ref('')

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const readCount = computed(() => notifications.value.filter(n => n.isRead).length)
const totalCount = computed(() => total.value)

const loadNotifications = async () => {
  loading.value = true
  try {
    let url = `/messages?page=${currentPage.value}&size=${pageSize.value}`
    if (filterType.value === 'unread') url += '&isRead=0'
    if (filterType.value === 'read') url += '&isRead=1'
    
    const res = await api.get(url)
    notifications.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await api.get('/messages/unread-count')
    // 更新全局未读数
    window.dispatchEvent(new CustomEvent('updateNotificationCount', { detail: res }))
  } catch (e) {
    console.error('Failed to load unread count:', e)
  }
}

const handleClick = async (item) => {
  if (!item.isRead) {
    await markAsRead(item)
  }
}

const markAsRead = async (item) => {
  try {
    await api.put(`/messages/${item.id}/read`)
    item.isRead = 1
    loadUnreadCount()
    ElMessage.success('已标记为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const markAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定将所有通知标记为已读？', '提示', { type: 'info' })
    await api.put('/messages/read-all')
    notifications.value.forEach(n => n.isRead = 1)
    loadUnreadCount()
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const deleteNotification = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除此通知？', '提示', { type: 'warning' })
    await api.delete(`/messages/${item.id}`)
    notifications.value = notifications.value.filter(n => n.id !== item.id)
    total.value--
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const deleteAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有已读通知？此操作不可恢复', '警告', { type: 'warning' })
    const readItems = notifications.value.filter(n => n.isRead)
    for (const item of readItems) {
      await api.delete(`/messages/${item.id}`)
    }
    notifications.value = notifications.value.filter(n => !n.isRead)
    ElMessage.success('已清空已读通知')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
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
  return date.toLocaleDateString('zh-CN')
}

const getTypeClass = (type) => {
  const map = {
    travel: 'blue',
    activity: 'green',
    exchange: 'orange',
    system: 'purple'
  }
  return map[type] || 'gray'
}

const getTypeIcon = (type) => {
  const map = {
    travel: Van,
    activity: Calendar,
    exchange: ShoppingCart,
    system: Setting
  }
  return map[type] || Bell
}

const getTypeName = (type) => {
  const map = {
    travel: '出行审核',
    activity: '活动参与',
    exchange: '兑换订单',
    system: '系统通知'
  }
  return map[type] || '系统通知'
}

const getTagType = (type) => {
  const map = {
    travel: '',
    activity: 'success',
    exchange: 'warning',
    system: 'info'
  }
  return map[type] || 'info'
}

onMounted(() => {
  loadNotifications()
  
  // 监听全局通知数量更新事件
  window.addEventListener('updateNotificationCount', (e) => {
    // 可以在这里处理全局通知数量更新
  })
})
</script>

<style scoped>
.notifications-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.blue { background: #dbeafe; color: #2563eb; }
.stat-icon.orange { background: #ffedd5; color: #ea580c; }
.stat-icon.green { background: #dcfce7; color: #16a34a; }
.stat-icon.purple { background: #f3e8ff; color: #9333ea; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.notification-list {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: all 0.2s;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background: #fffbeb;
}

.notification-item.unread:hover {
  background: #fef3c7;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.blue { background: #dbeafe; color: #2563eb; }
.notification-icon.green { background: #dcfce7; color: #16a34a; }
.notification-icon.orange { background: #ffedd5; color: #ea580c; }
.notification-icon.purple { background: #f3e8ff; color: #9333ea; }
.notification-icon.gray { background: #f3f4f6; color: #6b7280; }

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
}

.notification-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.notification-tags {
  display: flex;
  gap: 8px;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

/* 通知中心 - 靛蓝色系浅色按钮 */
:deep(.btn-indigo-light) {
  color: #4f46e5 !important;
  background: #eef2ff !important;
  border: 1px solid #c7d2fe !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-indigo-light:hover) {
  background: #e0e7ff !important;
  border-color: #a5b4fc !important;
}
:deep(.btn-gray-light) {
  color: #4b5563 !important;
  background: #f3f4f6 !important;
  border: 1px solid #d1d5db !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-gray-light:hover) {
  background: #e5e7eb !important;
  border-color: #9ca3af !important;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: #fff;
  border-radius: 0 0 16px 16px;
  border: 1px solid #e5e7eb;
  border-top: none;
}
</style>
