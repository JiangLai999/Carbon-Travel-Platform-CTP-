<template>
  <div class="users-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">维护平台生态，管理用户信息</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-info">
            <p class="stat-label">总用户数</p>
            <h3 class="stat-value">{{ stats.total.toLocaleString() }}</h3>
          </div>
          <div class="stat-icon green">
            <el-icon :size="20"><User /></el-icon>
          </div>
        </div>
        <div class="stat-trend" :class="stats.todayNew > 0 ? 'up' : 'neutral'">
          <el-icon><component :is="stats.todayNew > 0 ? 'Top' : 'Minus'" /></el-icon>
          <span>{{ stats.todayNew > 0 ? `+${stats.todayNew} 今日新增` : '暂无新增' }}</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-info">
            <p class="stat-label">今日新增</p>
            <h3 class="stat-value">{{ stats.todayNew }}</h3>
          </div>
          <div class="stat-icon orange">
            <el-icon :size="20"><Plus /></el-icon>
          </div>
        </div>
        <div class="stat-trend neutral">
          <el-icon><Clock /></el-icon>
          <span>今日注册用户</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-info">
            <p class="stat-label">活跃用户</p>
            <h3 class="stat-value">{{ stats.active.toLocaleString() }}</h3>
          </div>
          <div class="stat-icon blue">
            <el-icon :size="20"><Promotion /></el-icon>
          </div>
        </div>
        <div class="stat-trend up">
          <el-icon><CircleCheck /></el-icon>
          <span>正常使用中</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-info">
            <p class="stat-label">已封禁</p>
            <h3 class="stat-value">{{ stats.banned }}</h3>
          </div>
          <div class="stat-icon red">
            <el-icon :size="20"><Lock /></el-icon>
          </div>
        </div>
        <div class="stat-trend" :class="stats.banned > 0 ? 'warn' : 'up'">
          <el-icon><component :is="stats.banned > 0 ? 'Warning' : 'CircleCheck'" /></el-icon>
          <span>{{ stats.banned > 0 ? '需关注' : '无异常' }}</span>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索手机号/昵称/姓名/邮箱"
            :prefix-icon="Search"
            clearable
            style="width: 300px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="statusFilter" placeholder="所有状态" clearable style="width: 140px" @change="handleSearch">
            <el-option label="正常用户" value="active" />
            <el-option label="已禁用" value="banned" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="showAddDialog = true">新增用户</el-button>
      </div>

      <div class="table-section">
        <el-table :data="users" border style="width: 100%" v-loading="loading" @row-click="handleRowClick">
        <el-table-column label="用户" :width="columnWidths.user">
          <template #default="{ row }">
            <div class="user-cell compact">
              <el-avatar :size="isCompact ? 22 : 26" :src="row.avatar">{{ row.nickname?.charAt(0) || row.realName?.charAt(0) || 'U' }}</el-avatar>
              <span v-if="!isMobile" class="user-name-inline" :title="row.nickname || '未设置昵称'">{{ row.nickname || '未设置昵称' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" :width="columnWidths.phone" />
        <el-table-column v-if="columnWidths.name > 0" label="姓名" :width="columnWidths.name">
          <template #default="{ row }">
            <span class="real-name-text">{{ row.realName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="碳积分" :width="columnWidths.points" align="center">
          <template #default="{ row }">
            <span class="points-value">{{ row.points?.toLocaleString() || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="减碳" :width="columnWidths.carbon" align="center">
          <template #default="{ row }">
            <span class="carbon-value">{{ row.totalCarbon?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="columnWidths.date > 0" prop="createdAt" label="注册日期" :width="columnWidths.date" />
        <el-table-column label="状态" :width="columnWidths.status" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="getStatusClass(row.status)">
              <span class="status-dot"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="columnWidths.action" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button class="btn-blue-light" text size="small" @click.stop="handleViewDetail(row)">详情</el-button>
              <el-button v-if="!isMobile" class="btn-blue-light" text size="small" @click.stop="handleEdit(row)">编辑</el-button>
              <el-button 
                :class="row.status === 1 ? 'btn-gray-light' : 'btn-green-light'" 
                text 
                size="small" 
                :loading="operatingId === row.id" 
                @click.stop="row.status === 1 ? handleBan(row) : handleUnban(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <div class="table-footer">
        <p class="pagination-info">显示 {{ (page - 1) * size + 1 }} 到 {{ Math.min(page * size, total) }} 共 {{ total }} 条数据</p>
        <div class="pagination">
          <el-button :disabled="page === 1" @click="page--">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button type="primary">{{ page }}</el-button>
          <el-button v-for="p in visiblePages" :key="p" @click="page = p">{{ p }}</el-button>
          <span v-if="totalPages > 5" class="ellipsis">...</span>
          <el-button v-if="totalPages > 5" @click="page = totalPages">{{ totalPages }}</el-button>
          <el-button :disabled="page >= totalPages" @click="page++">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="550px">
      <el-form :model="editUser" label-width="90px">
        <el-form-item label="头像">
          <div class="avatar-upload" @click="triggerAvatarUpload">
            <el-avatar :size="60" :src="editUser.avatar">{{ editUser.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="avatar-upload-text">更换头像</div>
          </div>
          <input type="file" ref="avatarInput" style="display: none" accept="image/*" @change="handleAvatarChange" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editUser.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editUser.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="editUser.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="用户角色">
          <el-select v-model="editUser.role" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-select v-model="editUser.status" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="用户详情" width="700px" class="detail-dialog">
      <div class="user-detail" v-if="detailUser">
        <div class="detail-header">
          <el-avatar :size="80" :src="detailUser.avatar">{{ detailUser.nickname?.charAt(0) || 'U' }}</el-avatar>
          <div class="detail-info">
            <div class="detail-name-row">
              <h3>{{ detailUser.nickname || '未设置昵称' }}</h3>
              <span class="role-tag" :class="detailUser.role === 'admin' ? 'admin' : 'user'">
                {{ detailUser.role === 'admin' ? '管理员' : '普通用户' }}
              </span>
              <span class="status-tag" :class="detailUser.status === 1 ? 'active' : 'banned'">
                {{ detailUser.status === 1 ? '正常' : '已禁用' }}
              </span>
            </div>
            <p class="detail-id">用户编号: {{ detailUser.id }}</p>
          </div>
        </div>
        
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <div class="detail-section">
              <div class="detail-grid">
                <div class="detail-item">
                  <label>手机号</label>
                  <span>{{ detailUser.phone || '未绑定' }}</span>
                </div>
                <div class="detail-item">
                  <label>邮箱</label>
                  <span>{{ detailUser.email || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <label>真实姓名</label>
                  <span>{{ detailUser.realName || '未填写' }}</span>
                </div>
                <div class="detail-item">
                  <label>身份证号</label>
                  <span>{{ detailUser.idCard || '未填写' }}</span>
                </div>
                <div class="detail-item">
                  <label>注册时间</label>
                  <span>{{ detailUser.createdAt || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>更新时间</label>
                  <span>{{ detailUser.updatedAt || '未知' }}</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="数据统计" name="stats">
            <div class="detail-section">
              <div class="stats-cards">
                <div class="stats-card-item">
                  <div class="stats-card-icon green">💰</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.points?.toLocaleString() || 0 }}</span>
                    <span class="stats-card-label">可用积分</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon blue">🌿</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.totalCarbon?.toFixed(2) || '0.00' }}</span>
                    <span class="stats-card-label">减碳量(kg)</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon orange">🎯</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.activityCount || 0 }}</span>
                    <span class="stats-card-label">参与活动</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon purple">🗺️</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.travelCount || 0 }}</span>
                    <span class="stats-card-label">出行记录</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon pink">🎁</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.orderCount || 0 }}</span>
                    <span class="stats-card-label">兑换订单</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon cyan">📝</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.postCount || 0 }}</span>
                    <span class="stats-card-label">发帖数量</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon gold">🏆</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.rank || '-' }}</span>
                    <span class="stats-card-label">积分排名</span>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon silver">✅</div>
                  <div class="stats-card-info">
                    <span class="stats-card-value">{{ detailUser.approvedTravelCount || 0 }}</span>
                    <span class="stats-card-label">已审核出行</span>
                  </div>
                </div>
              </div>
              
              <div class="points-detail">
                <h4>积分明细</h4>
                <div class="points-row">
                  <div class="points-item">
                    <span class="points-label">累计积分</span>
                    <span class="points-value">{{ detailUser.totalPoints?.toLocaleString() || 0 }}</span>
                  </div>
                  <div class="points-item">
                    <span class="points-label">已使用积分</span>
                    <span class="points-value used">{{ detailUser.usedPoints?.toLocaleString() || 0 }}</span>
                  </div>
                  <div class="points-item">
                    <span class="points-label">可用积分</span>
                    <span class="points-value available">{{ detailUser.points?.toLocaleString() || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button class="btn-blue-solid" @click="handleEditFromDetail">编辑用户</el-button>
        <el-button class="btn-gray-solid" @click="handleResetFromFromDetail">重置密码</el-button>
        <el-button class="btn-gray-solid" v-if="detailUser?.status === 1" @click="handleBanFromDetail">禁用账号</el-button>
        <el-button class="btn-green-solid" v-else @click="handleUnbanFromDetail">启用账号</el-button>
      </template>
    </el-dialog>

    <!-- 新增用户弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增用户" width="500px">
      <el-form :model="newUser" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="newUser.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="newUser.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码（默认123456）" show-password />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="newUser.realName" placeholder="请输入真实姓名（选填）" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newUser.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser" :loading="adding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Plus, Top, Promotion, Lock, InfoFilled, Search, ArrowLeft, ArrowRight, Clock, CircleCheck, Warning, Minus } from '@element-plus/icons-vue'
import api, { getImageUrl } from '../../api'

const searchQuery = ref('')
const statusFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const detailUser = ref(null)
const detailTab = ref('basic')
const loading = ref(false)
const operatingId = ref(null)
const saving = ref(false)
const adding = ref(false)
const avatarInput = ref(null)

const stats = ref({
  total: 0,
  todayNew: 0,
  active: 0,
  banned: 0
})

const users = ref([])

const newUser = ref({
  phone: '',
  nickname: '',
  password: '',
  realName: '',
  email: ''
})

const editUser = ref({
  id: null,
  nickname: '',
  realName: '',
  idCard: '',
  email: '',
  avatar: '',
  status: 1,
  role: 'user'
})

const totalPages = computed(() => Math.ceil(total.value / size.value))

const visiblePages = computed(() => {
  const pages = []
  for (let i = 2; i <= Math.min(4, totalPages.value); i++) {
    pages.push(i)
  }
  return pages
})

const getStatusText = (status) => {
  const map = { 0: '已禁用', 1: '正常' }
  return map[status] || '正常'
}

const getStatusClass = (status) => {
  const map = { 0: 'banned', 1: 'active' }
  return map[status] || 'active'
}

const handleRowClick = (row) => {
  handleViewDetail(row)
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const loadStats = async () => {
  try {
    const res = await api.get('/admin/stats')
    if (res) {
      stats.value = {
        total: res.totalUsers || 0,
        todayNew: res.todayNewUsers || 0,
        active: res.activeUsers || res.totalUsers || 0,
        banned: res.bannedUsers || 0
      }
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    let url = `/admin/users?page=${page.value}&size=${size.value}`
    if (searchQuery.value) {
      url += `&keyword=${encodeURIComponent(searchQuery.value)}`
    }
    if (statusFilter.value) {
      const statusNum = { active: 1, banned: 0 }[statusFilter.value]
      if (statusNum !== undefined) {
        url += `&status=${statusNum}`
      }
    }
    const res = await api.get(url)
    if (res.records) {
      users.value = res.records.map(u => ({
        ...u,
        avatar: getImageUrl(u.avatar)
      }))
      total.value = res.total || 0
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (row) => {
  try {
    const res = await api.get(`/admin/users/${row.id}`)
    if (res) {
      detailUser.value = {
        ...res,
        avatar: getImageUrl(res.avatar)
      }
      detailTab.value = 'basic'
      showDetailDialog.value = true
    }
  } catch (e) {
    ElMessage.error('加载用户详情失败')
  }
}

const handleEditFromDetail = () => {
  if (detailUser.value) {
    handleEdit(detailUser.value)
    showDetailDialog.value = false
  }
}

const handleResetFromDetail = () => {
  if (detailUser.value) {
    handleResetPassword(detailUser.value)
  }
}

const handleBanFromDetail = () => {
  if (detailUser.value) {
    handleBan(detailUser.value)
    showDetailDialog.value = false
  }
}

const handleUnbanFromDetail = () => {
  if (detailUser.value) {
    handleUnban(detailUser.value)
    showDetailDialog.value = false
  }
}

const handleEdit = (row) => {
  editUser.value = {
    id: row.id,
    nickname: row.nickname || '',
    realName: row.realName || '',
    idCard: row.idCard || '',
    email: row.email || '',
    avatar: row.avatar || '',
    status: row.status === 1 ? 1 : 0,
    role: row.role || 'user'
  }
  showEditDialog.value = true
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await api.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.url) {
      editUser.value.avatar = getImageUrl(res.url)
      editUser.value._avatarPath = res.url
    }
  } catch (e) {
    ElMessage.error('上传头像失败')
  }
  
  event.target.value = ''
}

const handleSaveEdit = async () => {
  if (!editUser.value.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  
  saving.value = true
  try {
    const updateData = {
      nickname: editUser.value.nickname,
      realName: editUser.value.realName,
      idCard: editUser.value.idCard,
      email: editUser.value.email,
      status: editUser.value.status,
      role: editUser.value.role
    }
    
    if (editUser.value._avatarPath) {
      updateData.avatar = editUser.value._avatarPath
    }
    
    await api.put(`/admin/users/${editUser.value.id}`, updateData)
    ElMessage.success('用户信息已更新')
    showEditDialog.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确认重置该用户的密码？', '提示', { type: 'warning' })
    operatingId.value = row.id
    await api.put(`/admin/users/${row.id}/reset-password`)
    ElMessage.success('密码已重置为：123456')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  } finally {
    operatingId.value = null
  }
}

const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm('确认禁用该用户？禁用后用户将无法登录', '警告', { type: 'warning' })
    operatingId.value = row.id
    await api.put(`/admin/users/${row.id}/status`, null, { params: { status: 0 } })
    row.status = 0
    ElMessage.success('用户已禁用')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  } finally {
    operatingId.value = null
  }
}

const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm('确认启用该用户？', '提示', { type: 'info' })
    operatingId.value = row.id
    await api.put(`/admin/users/${row.id}/status`, null, { params: { status: 1 } })
    row.status = 1
    ElMessage.success('用户已启用')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  } finally {
    operatingId.value = null
  }
}

const handleAddUser = async () => {
  if (!newUser.value.phone || !newUser.value.nickname) {
    ElMessage.warning('请填写手机号和昵称')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(newUser.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  adding.value = true
  try {
    await api.post('/admin/users', {
      phone: newUser.value.phone,
      nickname: newUser.value.nickname,
      password: newUser.value.password || '123456',
      realName: newUser.value.realName,
      email: newUser.value.email
    })
    ElMessage.success('用户创建成功')
    showAddDialog.value = false
    newUser.value = { phone: '', nickname: '', password: '', realName: '', email: '' }
    page.value = 1
    loadUsers()
    loadStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    adding.value = false
  }
}

// 响应式表格列宽
const tableContainerWidth = ref(1200)
const isCompact = computed(() => tableContainerWidth.value < 1100)
const isMobile = computed(() => tableContainerWidth.value < 768)

const columnWidths = computed(() => {
  // 根据容器宽度动态计算，让列均匀分布占满整行
  const w = tableContainerWidth.value
  // 确保最小宽度，避免除0或负数
  const safeW = Math.max(w, 400)
  if (w < 768) {
    // 移动端：隐藏姓名和日期，操作列固定150，其余5列均分剩余
    const actionW = 150
    const remain = safeW - actionW
    const avg = Math.max(Math.floor(remain / 5), 60)
    return {
      user: avg,
      phone: avg,
      name: 0,
      points: avg,
      carbon: avg,
      date: 0,
      status: avg,
      action: actionW
    }
  }
  if (w < 1100) {
    // 紧凑屏：操作列固定180，其余7列均分剩余
    const actionW = 180
    const remain = safeW - actionW
    const avg = Math.max(Math.floor(remain / 7), 60)
    return {
      user: avg,
      phone: avg,
      name: avg,
      points: avg,
      carbon: avg,
      date: avg,
      status: avg,
      action: actionW
    }
  }
  // 大屏：操作列固定210，其余7列均分剩余
  const actionW = 210
  const remain = safeW - actionW
  const avg = Math.max(Math.floor(remain / 7), 80)
  return {
    user: avg,
    phone: avg,
    name: avg,
    points: avg,
    carbon: avg,
    date: avg,
    status: avg,
    action: actionW
  }
})

const updateTableWidth = () => {
  const container = document.querySelector('.users-page .table-section')
  if (container) {
    const width = container.clientWidth
    tableContainerWidth.value = width
    // eslint-disable-next-line no-console
    console.log('Table container width:', width)
  }
}

onMounted(() => {
  loadStats()
  loadUsers()
  // 多次延迟确保DOM完全渲染并获取准确宽度
  nextTick(() => {
    updateTableWidth()
  })
  setTimeout(() => {
    updateTableWidth()
  }, 100)
  setTimeout(() => {
    updateTableWidth()
  }, 500)
  window.addEventListener('resize', updateTableWidth)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateTableWidth)
})

watch([page, size], () => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 20px 24px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.green { background: #dcfce7; color: #16a34a; }
.stat-icon.blue { background: #dbeafe; color: #2563eb; }
.stat-icon.orange { background: #ffedd5; color: #ea580c; }
.stat-icon.red,
.stat-icon.warm { background: #fff7ed; color: #ea580c; }

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 12px;
}

.stat-trend.up { color: #16a34a; }
.stat-trend.warn { color: #ea580c; }
.stat-trend.neutral { color: #6b7280; }

.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-section {
  width: 100%;
  overflow-x: auto;
}

.table-section :deep(.el-table) {
  width: 100% !important;
}

.table-section :deep(.el-table__body-wrapper) {
  overflow-x: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  gap: 16px;
}

.search-bar {
  display: flex;
  gap: 12px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-cell.compact {
  gap: 6px;
}

.user-name-inline {
  font-weight: 600;
  color: #1f2937;
  font-size: 12px;
  max-width: 55px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .user-name-inline {
    max-width: 40px;
    font-size: 11px;
  }
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 13px;
  max-width: 85px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-id {
  font-size: 11px;
  color: #9ca3af;
}

.real-name-text {
  font-size: 13px;
  color: #4b5563;
  white-space: nowrap;
}

.points-value,
.carbon-value {
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
}

.points-value {
  color: #10b981;
}

.carbon-value {
  color: #059669;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.banned {
  background: #f3f4f6;
  color: #4b5563;
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-badge.active .status-dot { background: #16a34a; }
.status-badge.banned .status-dot { background: #9ca3af; }

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: nowrap;
}

/* 用户管理 - 蓝色系浅色按钮 */
:deep(.btn-blue-light) {
  color: #2563eb !important;
  background: #eff6ff !important;
  border: 1px solid #bfdbfe !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-blue-light:hover) {
  background: #dbeafe !important;
  border-color: #93c5fd !important;
}
:deep(.btn-gray-light) {
  color: #4b5563 !important;
  background: #f3f4f6 !important;
  border: 1px solid #d1d5db !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-gray-light:hover) {
  background: #e5e7eb !important;
  border-color: #9ca3af !important;
}
:deep(.btn-green-light) {
  color: #059669 !important;
  background: #ecfdf5 !important;
  border: 1px solid #a7f3d0 !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-green-light:hover) {
  background: #d1fae5 !important;
  border-color: #6ee7b7 !important;
}
:deep(.btn-blue-solid) {
  background: #3b82f6 !important;
  border-color: #3b82f6 !important;
  color: #fff !important;
}
:deep(.btn-blue-solid:hover) {
  background: #2563eb !important;
  border-color: #2563eb !important;
}
:deep(.btn-gray-solid) {
  background: #6b7280 !important;
  border-color: #6b7280 !important;
  color: #fff !important;
}
:deep(.btn-gray-solid:hover) {
  background: #4b5563 !important;
  border-color: #4b5563 !important;
}
:deep(.btn-green-solid) {
  background: #10b981 !important;
  border-color: #10b981 !important;
  color: #fff !important;
}
:deep(.btn-green-solid:hover) {
  background: #059669 !important;
  border-color: #059669 !important;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.pagination {
  display: flex;
  gap: 8px;
}

.ellipsis {
  display: flex;
  align-items: center;
  padding: 0 4px;
  color: #9ca3af;
}

/* 用户详情样式 */
.user-detail {
  padding: 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.detail-info {
  flex: 1;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-name-row h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.role-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.role-tag.admin {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.role-tag.user {
  background: #e0f2fe;
  color: #0369a1;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-tag.active {
  background: #dcfce7;
  color: #166534;
}

.status-tag.banned {
  background: #fff7ed;
  color: #c2410c;
}

.detail-id {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.detail-section {
  padding: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.detail-item label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.detail-item span {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 600px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
}

.stats-card-item {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stats-card-icon.green { background: #dcfce7; }
.stats-card-icon.blue { background: #dbeafe; }
.stats-card-icon.orange { background: #ffedd5; }
.stats-card-icon.purple { background: #f3e8ff; }
.stats-card-icon.pink { background: #fce7f3; }
.stats-card-icon.cyan { background: #cffafe; }
.stats-card-icon.gold { background: #fef3c7; }
.stats-card-icon.silver { background: #e5e7eb; }

.stats-card-info {
  display: flex;
  flex-direction: column;
}

.stats-card-value {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.stats-card-label {
  font-size: 12px;
  color: #6b7280;
}

.points-detail {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.points-detail h4 {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.points-row {
  display: flex;
  justify-content: space-around;
}

.points-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.points-label {
  font-size: 12px;
  color: #6b7280;
}

.points-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.points-value.used {
  color: #ea580c;
}

.points-value.available {
  color: #10b981;
}

.avatar-upload {
  cursor: pointer;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-upload:hover .el-avatar {
  opacity: 0.8;
}

.avatar-upload-text {
  font-size: 12px;
  color: #10b981;
}

.detail-dialog :deep(.el-dialog__body) {
  padding-top: 0;
}
</style>
