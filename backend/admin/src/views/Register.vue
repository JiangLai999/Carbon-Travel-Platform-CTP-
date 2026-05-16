<template>
  <div class="register-page">
    <header class="register-header">
      <div class="logo">
        <el-icon :size="28"><Key /></el-icon>
        <h2>管理端门户</h2>
      </div>
      <router-link to="/login" class="login-link">已有账号？去登录</router-link>
    </header>

    <main class="register-main">
      <div class="register-card">
        <div class="register-side">
          <h3>加入我们的管理团队</h3>
          <p>请提交您的真实信息。系统管理员将在2个工作日内完成审核。</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>安全加密传输</span>
            </div>
            <div class="feature-item">
              <el-icon><Clock /></el-icon>
              <span>快速审核通道</span>
            </div>
          </div>
        </div>

        <div class="register-form-wrapper">
          <div class="form-header">
            <h1>管理员入驻申请</h1>
            <p>请认真填写以下信息，带 * 为必填项</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" class="register-form" @submit.prevent="handleSubmit">
            <div class="form-row">
              <el-form-item prop="name" label="姓名">
                <el-input v-model="form.name" placeholder="请输入真实姓名" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="employeeId" label="工号">
                <el-input v-model="form.employeeId" placeholder="员工编号" :prefix-icon="Document" />
              </el-form-item>
            </div>

            <el-form-item prop="department" label="所属部门">
              <el-select v-model="form.department" placeholder="请选择所属部门" :prefix-icon="House">
                <el-option label="技术研发部" value="tech" />
                <el-option label="产品设计部" value="product" />
                <el-option label="人力资源部" value="hr" />
                <el-option label="财务管理部" value="finance" />
                <el-option label="运营中心" value="operations" />
              </el-select>
            </el-form-item>

            <el-form-item prop="phone" label="手机号">
              <el-input v-model="form.phone" placeholder="11位手机号码" :prefix-icon="Iphone" />
            </el-form-item>

            <el-form-item prop="code" label="验证码">
              <div class="code-input">
                <el-input v-model="form.code" placeholder="短信验证码" :prefix-icon="Key" />
                <el-button :disabled="countdown > 0" @click="sendCode">
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <div class="form-row">
              <el-form-item prop="password" label="设置密码">
                <el-input v-model="form.password" type="password" placeholder="8-16位字母数字" :prefix-icon="Lock" show-password />
              </el-form-item>
              <el-form-item prop="confirmPassword" label="确认密码">
                <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password />
              </el-form-item>
            </div>

            <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
              提交入驻申请
              <el-icon><ArrowRight /></el-icon>
            </el-button>

            <p class="agreement">
              提交即代表您同意我们的
              <a href="#">《管理员使用条款》</a> 和 <a href="#">《隐私权政策》</a>
            </p>
          </el-form>
        </div>
      </div>
    </main>

    <footer class="register-footer">
      <p>© 2024 管理员服务系统 · 安全稳定 · 实时监控</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, CircleCheck, Clock, User, Document, Iphone, Lock, Position, House, ArrowRight } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  name: '',
  employeeId: '',
  department: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  employeeId: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  department: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度为8-16位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== form.password) callback(new Error('两次密码不一致'))
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
    await api.post('/auth/send-code', { phone: form.phone })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await api.post('/auth/register', form)
      ElMessage.success('申请已提交，请等待审核')
      router.push('/login')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '提交失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f6f6;
}

.register-header {
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
  gap: 10px;
  color: #ec5b13;
}

.logo h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.login-link {
  font-size: 14px;
  color: #ec5b13;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

.register-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.register-card {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.register-side {
  width: 33.33%;
  padding: 40px 32px;
  background: linear-gradient(135deg, #ec5b13, #f97316);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.register-side h3 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 16px 0;
}

.register-side p {
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.6;
  margin: 0;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.register-form-wrapper {
  flex: 1;
  padding: 40px;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.form-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.register-form :deep(.el-input__wrapper),
.register-form :deep(.el-select) {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: none;
}

.register-form :deep(.el-input__wrapper:focus-within),
.register-form :deep(.el-select:focus-within .el-input__wrapper) {
  border-color: #ec5b13;
  box-shadow: 0 0 0 3px rgba(236, 91, 19, 0.1);
}

.register-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.register-form :deep(.el-form-item__label::before) {
  content: '';
}

.register-form :deep(.el-form-item.is-required .el-form-item__label::after) {
  content: ' *';
  color: #ec5b13;
}

.code-input {
  display: flex;
  gap: 12px;
}

.code-input .el-input {
  flex: 1;
}

.code-input .el-button {
  white-space: nowrap;
  background: rgba(236, 91, 19, 0.1);
  color: #ec5b13;
  border: 1px solid rgba(236, 91, 19, 0.3);
}

.code-input .el-button:hover {
  background: rgba(236, 91, 19, 0.2);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
}

.agreement {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  margin: 16px 0 0 0;
}

.agreement a {
  color: #ec5b13;
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
}

.register-footer {
  padding: 24px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

@media (max-width: 768px) {
  .register-side {
    display: none;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
