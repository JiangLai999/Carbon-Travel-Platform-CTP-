<template>
  <div class="reset-page">
    <header class="reset-header">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="24"><Key /></el-icon>
        </div>
        <h2>管理后台 <span class="divider">|</span> <span class="subtitle">安全中心</span></h2>
      </div>
      <div class="header-actions">
        <button class="icon-btn">
          <el-icon :size="20"><Bell /></el-icon>
        </button>
        <button class="icon-btn">
          <el-icon :size="20"><User /></el-icon>
        </button>
      </div>
    </header>

    <main class="reset-main">
      <div class="reset-container">
        <div class="reset-title">
          <h1>重置您的密码</h1>
          <p>为了您的账号安全，请先通过绑定的手机号完成身份验证</p>
        </div>

        <div class="reset-card">
          <div class="steps">
            <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
              <div class="step-icon">
                <el-icon v-if="currentStep > 1"><Check /></el-icon>
                <el-icon v-else><UserFilled /></el-icon>
              </div>
              <div class="step-info">
                <span class="step-label">第一步</span>
                <span class="step-name">验证身份</span>
              </div>
            </div>
            <div class="step-line" :class="{ active: currentStep > 1 }"></div>
            <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
              <div class="step-icon">
                <el-icon v-if="currentStep > 2"><Check /></el-icon>
                <el-icon v-else><Lock /></el-icon>
              </div>
              <div class="step-info">
                <span class="step-label">第二步</span>
                <span class="step-name">设置新密码</span>
              </div>
            </div>
            <div class="step-line" :class="{ active: currentStep > 2 }"></div>
            <div class="step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
              <div class="step-icon">
                <el-icon v-if="currentStep > 3"><Check /></el-icon>
                <el-icon v-else><CircleCheck /></el-icon>
              </div>
              <div class="step-info">
                <span class="step-label">第三步</span>
                <span class="step-name">完成重置</span>
              </div>
            </div>
          </div>

          <!-- Step 1: Verify Identity -->
          <div v-if="currentStep === 1" class="step-content">
            <el-form ref="formRef" :model="form" :rules="rules" class="reset-form">
              <el-form-item prop="phone">
                <label class="form-label">注册手机号</label>
                <el-input
                  v-model="form.phone"
                  placeholder="请输入绑定的11位手机号"
                  size="large"
                  :prefix-icon="Iphone"
                />
              </el-form-item>

              <el-form-item prop="code">
                <label class="form-label">短信验证码</label>
                <div class="code-input">
                  <el-input
                    v-model="form.code"
                    placeholder="6位验证码"
                    size="large"
                    :prefix-icon="Key"
                    maxlength="6"
                  />
                  <el-button
                    size="large"
                    :disabled="countdown > 0"
                    @click="sendCode"
                    class="code-btn"
                  >
                    {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="nextStep"
              >
                下一步
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-form>
            <router-link to="/login" class="back-link">返回登录页面</router-link>
          </div>

          <!-- Step 2: Set New Password -->
          <div v-if="currentStep === 2" class="step-content">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="reset-form">
              <el-form-item prop="password">
                <label class="form-label">设置新密码</label>
                <el-input
                  v-model="passwordForm.password"
                  type="password"
                  placeholder="请输入新密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <label class="form-label">确认新密码</label>
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="submitReset"
                :loading="loading"
              >
                确认重置
                <el-icon><Check /></el-icon>
              </el-button>
            </el-form>
            <button class="back-link" @click="currentStep = 1">返回上一步</button>
          </div>

          <!-- Step 3: Success -->
          <div v-if="currentStep === 3" class="step-content success-content">
            <div class="success-icon">
              <el-icon :size="64"><CircleCheck /></el-icon>
            </div>
            <h2>密码重置成功</h2>
            <p>您的密码已成功重置，请使用新密码登录</p>
            <el-button type="primary" size="large" @click="router.push('/login')">
              立即登录
            </el-button>
          </div>
        </div>

        <div class="security-notice">
          <div class="notice-item">
            <el-icon><Check /></el-icon>
            <span>SSL 安全加密</span>
          </div>
          <div class="notice-item">
            <el-icon><Check /></el-icon>
            <span>多重身份验证</span>
          </div>
          <div class="notice-item">
            <el-icon><Check /></el-icon>
            <span>数据安全保障</span>
          </div>
        </div>
      </div>
    </main>

    <footer class="reset-footer">
      <p>© 2024 管理后台系统 - 版权所有</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, Bell, User, Check, UserFilled, Lock, CircleCheck, Iphone, ArrowRight } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const currentStep = ref(1)
const loading = ref(false)
const countdown = ref(0)
const formRef = ref(null)
const passwordFormRef = ref(null)

const form = reactive({
  phone: '',
  code: ''
})

const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const passwordRules = {
  password: [
    { required: true, message: '请设置新密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度为8-16位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== passwordForm.password) callback(new Error('两次密码不一致'))
      else callback()
    }, trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    const res = await api.post('/auth/send-code', { phone: form.phone })
    ElMessage.success('验证码已发送')
    
    // 开发模式下自动填充验证码
    if (res && res.code) {
      form.code = res.code
      ElMessage.info(`验证码已自动填充: ${res.code}`)
    }
    
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试')
  }
}

const nextStep = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      currentStep.value = 2
    }
  })
}

const submitReset = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await api.post('/auth/reset-password', {
        phone: form.phone,
        code: form.code,
        newPassword: passwordForm.password
      })
      currentStep.value = 3
      ElMessage.success('密码重置成功')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '重置失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f6f6;
}

.reset-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 50;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: #ec5b13;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.logo h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.divider {
  color: #d1d5db;
  margin: 0 12px;
}

.subtitle {
  font-weight: 400;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #f3f4f6;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #e5e7eb;
  color: #ec5b13;
}

.reset-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
}

.reset-container {
  width: 100%;
  max-width: 800px;
}

.reset-title {
  text-align: center;
  margin-bottom: 32px;
}

.reset-title h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.reset-title p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.reset-card {
  background: #fff;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 48px;
  position: relative;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

.step-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 20px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.step.active .step-icon {
  background: #ec5b13;
  border-color: #ec5b13;
  color: #fff;
  box-shadow: 0 4px 12px rgba(236, 91, 19, 0.3);
}

.step.completed .step-icon {
  background: #16a34a;
  border-color: #16a34a;
  color: #fff;
}

.step-info {
  text-align: center;
}

.step-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #ec5b13;
  margin-bottom: 4px;
}

.step:not(.active) .step-label {
  color: #9ca3af;
}

.step-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.step:not(.active) .step-name {
  color: #9ca3af;
}

.step-line {
  position: absolute;
  top: 24px;
  left: 0;
  right: 0;
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.step-line.active {
  background: #16a34a;
}

.step-content {
  max-width: 400px;
  margin: 0 auto;
}

.reset-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.reset-form :deep(.el-input__wrapper) {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: none;
}

.reset-form :deep(.el-input__wrapper:focus-within) {
  border-color: #ec5b13;
  box-shadow: 0 0 0 3px rgba(236, 91, 19, 0.1);
}

.code-input {
  display: flex;
  gap: 12px;
}

.code-input .el-input {
  flex: 1;
}

.code-btn {
  white-space: nowrap;
  background: rgba(236, 91, 19, 0.1);
  color: #ec5b13;
  border: 1px solid rgba(236, 91, 19, 0.2);
  font-weight: 600;
}

.code-btn:hover:not(:disabled) {
  background: rgba(236, 91, 19, 0.2);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
}

.back-link {
  display: block;
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
}

.back-link:hover {
  color: #ec5b13;
}

.success-content {
  text-align: center;
  padding: 24px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #dcfce7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: #16a34a;
}

.success-content h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.success-content p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px 0;
}

.security-notice {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 24px;
  opacity: 0.6;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.reset-footer {
  padding: 24px;
  text-align: center;
  border-top: 1px solid #e5e7eb;
}

.reset-footer p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}
</style>
