<template>
  <div class="login-page">
    <div class="login-bg">
      <img src="https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?w=1920&q=80" alt="background" />
      <div class="login-overlay"></div>
    </div>
    
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-circle">
            <el-icon :size="40"><Present /></el-icon>
          </div>
          <h1 class="login-title">低碳管理系统</h1>
          <p class="login-subtitle">专业、高效、环保的数字化能源管理</p>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入您的账号"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入登录密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <router-link to="/reset-password" class="forgot-link">忘记密码？</router-link>
          </div>
          
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            立即登录
          </el-button>
        </el-form>
        
        <div class="login-footer">
          <p>系统遇到问题？</p>
          <a href="#" class="support-link">
            <el-icon><Service /></el-icon>
            联系超级管理员
          </a>
        </div>
      </div>
      
      <div class="login-copyright">
        <p>© 低碳管理系统 版权所有</p>
        <p>助力碳中和，共建绿色未来</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Present, User, Lock, Service } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await api.post('/auth/admin/login', form)
      localStorage.setItem('token', res.token)
      localStorage.setItem('userRole', res.role || 'admin')
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      ElMessage.error(e.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.login-bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(2px);
}

.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 16px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.login-header {
  text-align: center;
  padding: 40px 32px 32px;
}

.logo-circle {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: rgba(236, 91, 19, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ec5b13;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.login-form {
  padding: 0 32px 32px;
}

.login-form :deep(.el-input__wrapper) {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: none;
}

.login-form :deep(.el-input__wrapper:focus-within) {
  border-color: #ec5b13;
  box-shadow: 0 0 0 3px rgba(236, 91, 19, 0.1);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  color: #ec5b13;
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: #ec5b13;
  border-color: #ec5b13;
}

.login-btn:hover {
  background: #f97316;
  border-color: #f97316;
}

.login-footer {
  text-align: center;
  padding: 24px 32px;
  border-top: 1px solid #e5e7eb;
}

.login-footer p {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.support-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #ec5b13;
  text-decoration: none;
  margin-top: 4px;
}

.support-link:hover {
  text-decoration: underline;
}

.login-copyright {
  text-align: center;
  margin-top: 32px;
  color: rgba(255, 255, 255, 0.7);
}

.login-copyright p {
  font-size: 12px;
  margin-bottom: 4px;
}
</style>
