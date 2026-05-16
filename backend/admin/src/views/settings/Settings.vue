<template>
  <div class="settings-page">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-subtitle">管理您的账户和系统偏好设置</p>
    </div>

    <div class="settings-content">
      <div class="settings-nav">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          class="nav-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <div class="settings-panel">
        <!-- Profile Settings -->
        <div v-if="activeTab === 'profile'" class="panel-content">
          <h3>个人信息</h3>
          <el-form :model="profileForm" label-width="100px" class="settings-form">
            <el-form-item label="头像">
              <el-avatar :size="80" :src="profileForm.avatar">{{ profileForm.name?.charAt(0) }}</el-avatar>
              <el-button class="btn-slate-light" text style="margin-left: 16px">更换头像</el-button>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="profileForm.name" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" disabled />
            </el-form-item>
            <el-form-item>
              <el-button class="btn-slate-solid" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Security Settings -->
        <div v-if="activeTab === 'security'" class="panel-content">
          <h3>安全设置</h3>
          <div class="security-item">
            <div class="security-info">
              <h4>登录密码</h4>
              <p>定期更换密码可以提高账户安全性</p>
            </div>
            <el-button class="btn-slate-light" text @click="showPasswordDialog = true">修改密码</el-button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>两步验证</h4>
              <p>启用两步验证为您的账户添加额外保护</p>
            </div>
            <el-switch v-model="securityForm.twoFactor" />
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>登录通知</h4>
              <p>当有新设备登录时发送通知</p>
            </div>
            <el-switch v-model="securityForm.loginNotify" />
          </div>
        </div>

        <!-- Notification Settings -->
        <div v-if="activeTab === 'notification'" class="panel-content">
          <h3>通知设置</h3>
          <el-form :model="notificationForm" label-width="140px" class="settings-form">
            <el-form-item label="系统通知">
              <el-switch v-model="notificationForm.system" />
            </el-form-item>
            <el-form-item label="审核提醒">
              <el-switch v-model="notificationForm.review" />
            </el-form-item>
            <el-form-item label="用户反馈">
              <el-switch v-model="notificationForm.feedback" />
            </el-form-item>
            <el-form-item label="活动通知">
              <el-switch v-model="notificationForm.activity" />
            </el-form-item>
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button class="btn-slate-solid" @click="saveNotification">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- System Settings -->
        <div v-if="activeTab === 'system'" class="panel-content">
          <h3>系统配置</h3>
          <el-form :model="systemForm" label-width="140px" class="settings-form">
            <el-form-item label="站点名称">
              <el-input v-model="systemForm.siteName" placeholder="请输入站点名称" />
            </el-form-item>
            <el-form-item label="维护模式">
              <el-switch v-model="systemForm.maintenance" />
              <span class="form-hint">开启后普通用户将无法访问系统</span>
            </el-form-item>
            <el-form-item label="数据备份">
              <el-button @click="backupData">立即备份</el-button>
              <span class="form-hint">上次备份：{{ systemForm.lastBackup }}</span>
            </el-form-item>
            <el-form-item>
              <el-button class="btn-slate-solid" @click="saveSystem">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- Password Dialog -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button class="btn-slate-solid" @click="changePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Bell, Setting } from '@element-plus/icons-vue'
import api from '../../api'

const activeTab = ref('profile')

const tabs = [
  { key: 'profile', label: '个人信息', icon: 'User' },
  { key: 'security', label: '安全设置', icon: 'Lock' },
  { key: 'notification', label: '通知设置', icon: 'Bell' },
  { key: 'system', label: '系统配置', icon: 'Setting' }
]

const profileForm = reactive({
  avatar: '',
  name: '',
  email: '',
  phone: ''
})

const securityForm = reactive({
  twoFactor: false,
  loginNotify: true
})

const notificationForm = reactive({
  system: true,
  review: true,
  feedback: false,
  activity: true,
  email: false
})

const systemForm = reactive({
  siteName: '低碳出行管理平台',
  maintenance: false,
  lastBackup: '从未'
})

const showPasswordDialog = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loadProfile = async () => {
  try {
    const res = await api.get('/admin/profile')
    if (res) {
      profileForm.name = res.nickname || res.name || '管理员'
      profileForm.email = res.email || ''
      profileForm.phone = res.phone || ''
      profileForm.avatar = res.avatar || ''
    }
  } catch (e) {
    console.error('Failed to load profile:', e)
  }
}

const saveProfile = async () => {
  try {
    await api.put('/admin/profile', {
      nickname: profileForm.name,
      avatar: profileForm.avatar
    })
    ElMessage.success('个人信息已保存')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

const saveNotification = () => {
  ElMessage.success('通知设置已保存')
}

const saveSystem = () => {
  ElMessage.success('系统配置已保存')
}

const backupData = () => {
  ElMessage.success('数据备份已开始')
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }
  try {
    await api.put('/admin/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '密码修改失败')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.settings-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.settings-content {
  display: flex;
  gap: 24px;
}

.settings-nav {
  width: 220px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  height: fit-content;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.nav-item.active {
  background: rgba(236, 91, 19, 0.1);
  color: #ec5b13;
}

/* 系统设置 - 石板灰色系浅色按钮 */
:deep(.btn-slate-light) {
  color: #475569 !important;
  background: #f8fafc !important;
  border: 1px solid #cbd5e1 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-slate-light:hover) {
  background: #f1f5f9 !important;
  border-color: #94a3b8 !important;
}
:deep(.btn-slate-solid) {
  background: #64748b !important;
  border-color: #64748b !important;
  color: #fff !important;
}
:deep(.btn-slate-solid:hover) {
  background: #475569 !important;
  border-color: #475569 !important;
}

.settings-panel {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.panel-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 24px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.settings-form {
  max-width: 500px;
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f3f4f6;
}

.security-item:last-child {
  border-bottom: none;
}

.security-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.security-info p {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

@media (max-width: 768px) {
  .settings-content {
    flex-direction: column;
  }
  
  .settings-nav {
    width: 100%;
    display: flex;
    overflow-x: auto;
    padding: 8px;
  }
  
  .nav-item {
    flex-shrink: 0;
    margin-bottom: 0;
    margin-right: 8px;
  }
}
</style>
