<template>
  <div class="activity-page">
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">活动发布管理</h1>
        <p class="page-subtitle">创建、编辑并管理您的站内线上及线下活动</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">新建活动</el-button>
    </div>

    <div class="activity-card">
      <el-tabs v-model="activeTab" class="activity-tabs">
        <el-tab-pane label="全部活动" name="all" />
        <el-tab-pane label="进行中" name="ongoing" />
        <el-tab-pane label="草稿箱" name="draft" />
        <el-tab-pane label="已结束" name="ended" />
      </el-tabs>

      <el-table :data="filteredActivities" border style="width: 100%">
        <el-table-column label="活动名称(ID)" min-width="280">
          <template #default="{ row }">
            <div class="activity-name-cell">
              <span class="activity-name">{{ row.name }}</span>
              <span class="activity-id">{{ row.id }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动类型" width="140" align="center">
          <template #default="{ row }">
            <span class="type-tag" :class="getTypeClass(row.type)">{{ row.type || '线下活动' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <span>{{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.statusClass">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button class="btn-purple-light" text size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button class="btn-cyan-light" text size="small" @click="handlePreview(row)">预览</el-button>
              <el-button class="btn-red-light" text size="small" @click="handleDelete(row)">删除</el-button>
              <el-button v-if="row.statusRaw === 'draft'" class="btn-green-light" text size="small" @click="handlePublish(row)">立即发布</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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

    <div class="proof-section">
      <div class="section-header">
        <div class="section-title-area">
          <h2 class="section-title">报名凭证审核</h2>
          <span class="pending-badge">{{ pendingCount }} 待审核</span>
        </div>
        <div class="section-filters">
          <el-select v-model="proofActivityFilter" placeholder="选择活动筛选" clearable style="width: 180px" @change="loadProofs">
            <el-option label="全部活动" value="" />
            <el-option v-for="a in activities" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
          <el-select v-model="proofStatusFilter" placeholder="审核状态" clearable style="width: 120px" @change="loadProofs">
            <el-option label="全部" value="" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </div>
      </div>

      <div class="proof-grid">
        <div v-for="proof in filteredProofs" :key="proof.id" class="proof-card">
          <div class="proof-image" @click="handleImagePreview(proof.image)">
            <img :src="proof.image" :alt="proof.activityType" />
            <div class="image-overlay">
              <el-icon :size="24"><ZoomIn /></el-icon>
            </div>
            <div class="proof-status-overlay" :class="getProofStatusClass(proof.status)">
              {{ getProofStatusText(proof.status) }}
            </div>
          </div>
          <div class="proof-content">
            <div class="proof-activity-type">
              <span class="type-tag" :class="proof.activityType?.includes('线上') ? 'online' : 'offline'">{{ proof.activityType }}</span>
            </div>
            <div class="proof-user">
              <el-avatar :size="32" :src="proof.avatar">{{ (proof.user || 'U').charAt(0) }}</el-avatar>
              <div class="user-info">
                <span class="user-name">{{ proof.user }}</span>
                <span class="submit-time">{{ proof.submitTime }}</span>
              </div>
            </div>
            <div class="proof-description">{{ proof.description }}</div>
            <div class="proof-actions">
              <el-button class="btn-green-light" size="small" @click="handleApproveProof(proof)" :disabled="proof.status !== 0">通过</el-button>
              <el-button class="btn-orange-light" size="small" @click="handleRejectProof(proof)" :disabled="proof.status !== 0">驳回</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="proof-pagination">
        <el-pagination
          v-model:current-page="proofPage"
          :page-size="6"
          :total="proofTotal"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="showAddDialog" :title="isEditing ? '编辑活动' : '新建活动'" width="600px">
      <el-form :model="activityForm" label-width="100px">
        <el-form-item label="活动名称">
          <el-input v-model="activityForm.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="activityForm.type" placeholder="请选择活动类型" style="width: 100%">
            <el-option label="线上工作坊" value="线上工作坊" />
            <el-option label="线下峰会" value="线下峰会" />
            <el-option label="线上讲座" value="线上讲座" />
            <el-option label="线下活动" value="线下活动" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker
            v-model="activityForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="奖励积分">
          <el-input-number v-model="activityForm.rewardPoints" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="参与要求">
          <el-input v-model="activityForm.requirement" placeholder="请输入参与要求" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="activityForm.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item label="活动图片">
          <div class="upload-section">
            <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <div v-if="activityForm.imageUrl" class="uploaded-image">
                <img :src="activityForm.imageUrl" alt="活动图片" />
                <div class="image-actions">
                  <el-icon @click.stop="removeImage"><Delete /></el-icon>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>点击上传图片</span>
              </div>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最大 5MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button @click="handleSaveDraft">存为草稿</el-button>
        <el-button class="btn-green-solid" @click="handleSaveActivity">{{ isEditing ? '保存修改' : '立即发布' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showImagePreview" title="凭证预览" width="600px">
      <img :src="previewImage" style="width: 100%" alt="凭证预览" />
    </el-dialog>

    <el-dialog v-model="showPreviewDialog" title="活动预览" width="600px">
      <div v-if="previewActivity" class="preview-content">
        <h3>{{ previewActivity.title }}</h3>
        <div class="preview-info">
          <div class="preview-item">
            <span class="label">活动类型：</span>
            <span>{{ previewActivity.type || '线下活动' }}</span>
          </div>
          <div class="preview-item">
            <span class="label">活动时间：</span>
            <span>{{ formatDate(previewActivity.startDate) }} ~ {{ formatDate(previewActivity.endDate) }}</span>
          </div>
          <div class="preview-item">
            <span class="label">奖励积分：</span>
            <span>{{ previewActivity.rewardPoints || 0 }} 积分</span>
          </div>
          <div class="preview-item">
            <span class="label">参与要求：</span>
            <span>{{ previewActivity.requirement || '无' }}</span>
          </div>
          <div class="preview-item">
            <span class="label">活动状态：</span>
            <span class="status-badge" :class="previewActivity.statusClass">{{ previewActivity.status }}</span>
          </div>
        </div>
        <div class="preview-description">
          <h4>活动描述</h4>
          <p>{{ previewActivity.description || '暂无描述' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, ArrowRight, ZoomIn, Delete } from '@element-plus/icons-vue'
import api, { getImageUrl } from '../../api'

const uploadUrl = '/api/upload/image'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const activeTab = ref('all')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showAddDialog = ref(false)
const showImagePreview = ref(false)
const previewImage = ref('')
const proofActivityFilter = ref('')
const proofPage = ref(1)
const proofTotal = ref(0)
const pendingCount = ref(0)
const loading = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const showPreviewDialog = ref(false)
const previewActivity = ref(null)
const proofStatusFilter = ref(0)

const activities = ref([])

const proofs = ref([])

const activityForm = ref({
  title: '',
  type: '',
  dateRange: [],
  rewardPoints: 0,
  requirement: '',
  description: '',
  imageUrl: ''
})

const totalPages = computed(() => Math.ceil(total.value / size.value))

const visiblePages = computed(() => {
  const pages = []
  for (let i = 2; i <= Math.min(4, totalPages.value); i++) {
    pages.push(i)
  }
  return pages
})

const filteredActivities = computed(() => {
  if (activeTab.value === 'all') return activities.value
  return activities.value.filter(a => a.statusRaw === activeTab.value)
})

const filteredProofs = computed(() => {
  let result = proofs.value
  if (proofActivityFilter.value) {
    result = result.filter(p => p.activityId === proofActivityFilter.value)
  }
  if (proofStatusFilter.value !== '' && proofStatusFilter.value !== null) {
    result = result.filter(p => p.status === proofStatusFilter.value)
  }
  return result
})

const handleEdit = (row) => {
  isEditing.value = true
  editingId.value = row.id
  activityForm.value = {
    title: row.title || row.name || '',
    type: row.type || '',
    dateRange: row.startDate && row.endDate ? [row.startDate, row.endDate] : [],
    rewardPoints: row.rewardPoints || 0,
    requirement: row.requirement || '',
    description: row.description || '',
    imageUrl: row.imageUrl || ''
  }
  showAddDialog.value = true
}

const handlePreview = (row) => {
  previewActivity.value = row
  showPreviewDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该活动吗？', '提示', { type: 'warning' })
    await api.delete(`/admin/activities/${row.id}`)
    ElMessage.success('删除成功')
    loadActivities()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确认发布该活动吗？', '提示', { type: 'info' })
    await api.put(`/admin/activities/${row.id}`, { status: 1 })
    ElMessage.success('发布成功')
    loadActivities()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  }
}

const handleImagePreview = (image) => {
  previewImage.value = image
  showImagePreview.value = true
}

const handleSaveDraft = async () => {
  if (!activityForm.value.title) {
    ElMessage.warning('请输入活动名称')
    return
  }
  try {
    const data = {
      title: activityForm.value.title,
      type: activityForm.value.type || '线下活动',
      description: activityForm.value.description,
      requirement: activityForm.value.requirement,
      rewardPoints: activityForm.value.rewardPoints,
      startDate: activityForm.value.dateRange?.[0],
      endDate: activityForm.value.dateRange?.[1],
      imageUrl: activityForm.value.imageUrl,
      status: 0
    }
    if (isEditing.value) {
      await api.put(`/admin/activities/${editingId.value}`, data)
      ElMessage.success('草稿已保存')
    } else {
      await api.post('/admin/activities', data)
      ElMessage.success('已保存为草稿')
    }
    showAddDialog.value = false
    resetForm()
    loadActivities()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

const handleSaveActivity = async () => {
  if (!activityForm.value.title) {
    ElMessage.warning('请输入活动名称')
    return
  }
  if (!activityForm.value.dateRange?.length) {
    ElMessage.warning('请选择活动时间')
    return
  }
  try {
    const data = {
      title: activityForm.value.title,
      type: activityForm.value.type || '线下活动',
      description: activityForm.value.description,
      requirement: activityForm.value.requirement,
      rewardPoints: activityForm.value.rewardPoints,
      startDate: activityForm.value.dateRange[0],
      endDate: activityForm.value.dateRange[1],
      imageUrl: activityForm.value.imageUrl,
      status: 1
    }
    if (isEditing.value) {
      await api.put(`/admin/activities/${editingId.value}`, data)
      ElMessage.success('活动已更新')
    } else {
      await api.post('/admin/activities', data)
      ElMessage.success('活动发布成功')
    }
    showAddDialog.value = false
    resetForm()
    loadActivities()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  }
}

const resetForm = () => {
  activityForm.value = {
    title: '',
    type: '',
    dateRange: [],
    rewardPoints: 0,
    requirement: '',
    description: '',
    imageUrl: ''
  }
  isEditing.value = false
  editingId.value = null
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    activityForm.value.imageUrl = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const removeImage = () => {
  activityForm.value.imageUrl = ''
}

const openAddDialog = () => {
  resetForm()
  showAddDialog.value = true
}

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/activities', { params: { page: page.value, size: size.value } })
    if (res.records) {
      activities.value = res.records.map(a => {
        const now = new Date()
        const startDate = a.startDate ? new Date(a.startDate) : null
        const endDate = a.endDate ? new Date(a.endDate) : null
        
        let statusRaw = 'draft'
        let statusText = '草稿'
        let statusClass = 'draft'
        
        if (a.status === 1) {
          if (endDate && endDate < now) {
            statusRaw = 'ended'
            statusText = '已结束'
            statusClass = 'ended'
          } else {
            statusRaw = 'ongoing'
            statusText = '进行中'
            statusClass = 'enrolling'
          }
        } else if (a.status === 0) {
          statusRaw = 'draft'
          statusText = '草稿'
          statusClass = 'draft'
        }
        
        return {
          ...a,
          name: a.title,
          imageUrl: getImageUrl(a.imageUrl),
          statusRaw,
          status: statusText,
          statusClass,
          typeClass: getTypeClass(a.type)
        }
      })
      total.value = res.total || 0
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载活动失败')
  } finally {
    loading.value = false
  }
}

const getTypeClass = (type) => {
  if (!type) return 'offline'
  if (type.includes('线上')) return 'online'
  return 'offline'
}

const getProofStatusClass = (status) => {
  if (status === 0) return 'pending'
  if (status === 1) return 'approved'
  if (status === 2) return 'rejected'
  return ''
}

const getProofStatusText = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return ''
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const loadProofs = async () => {
  try {
    const res = await api.get('/admin/activities/participations', { 
      params: { page: proofPage.value, size: 50, activityId: proofActivityFilter.value || undefined }
    })
    if (res.records) {
      proofs.value = res.records.map(p => ({
        id: p.id,
        activityId: p.activityId,
        activityType: p.activityTitle || '活动参与',
        user: p.userName || p.nickname || `用户${p.userId}`,
        avatar: getImageUrl(p.avatar),
        submitTime: formatDateTime(p.createdAt),
        image: getImageUrl(p.evidenceUrl) || 'https://via.placeholder.com/300x180?text=凭证图片',
        description: p.reviewComment || '参与活动凭证',
        status: p.status || 0
      }))
      proofTotal.value = res.total || 0
    }
    const statsRes = await api.get('/admin/stats')
    if (statsRes) {
      pendingCount.value = statsRes.pendingProofs || 0
    }
  } catch (e) {
    console.error('Failed to load proofs:', e)
  }
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleApproveProof = async (proof) => {
  try {
    await ElMessageBox.confirm('确认审核通过该凭证？', '提示', { type: 'info' })
    await api.post(`/admin/activities/participations/${proof.id}/review`, null, {
      params: { status: 1, comment: '审核通过' }
    })
    ElMessage.success('审核通过')
    loadProofs()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '审核失败')
  }
}

const handleRejectProof = async (proof) => {
  try {
    await ElMessageBox.confirm('确认驳回该凭证？', '提示', { type: 'warning' })
    await api.post(`/admin/activities/participations/${proof.id}/review`, null, {
      params: { status: 2, comment: '凭证不符合要求' }
    })
    ElMessage.success('已驳回')
    loadProofs()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '驳回失败')
  }
}

onMounted(() => {
  loadActivities()
  loadProofs()
})

watch([page, activeTab], () => {
  loadActivities()
})

watch(proofPage, () => {
  loadProofs()
})
</script>

<style scoped>
.activity-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
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

.activity-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  margin-bottom: 24px;
}

.activity-tabs {
  padding: 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.activity-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.activity-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.activity-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 16px 0;
  margin-right: 32px;
}

.activity-tabs :deep(.el-tabs__item.is-active) {
  color: #ec5b13;
}

.activity-tabs :deep(.el-tabs__active-bar) {
  background-color: #ec5b13;
}

.activity-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-name {
  font-weight: 600;
  color: #1f2937;
}

.activity-id {
  font-size: 12px;
  color: #9ca3af;
}

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.type-tag.online {
  background: #dbeafe;
  color: #2563eb;
}

.type-tag.offline {
  background: #fef3c7;
  color: #d97706;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.enrolling {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.ongoing {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.draft {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.ended {
  background: #fff7ed;
  color: #ea580c;
}

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

/* 活动管理 - 紫色系浅色按钮 */
:deep(.btn-purple-light) {
  color: #7c3aed !important;
  background: #f5f3ff !important;
  border: 1px solid #ddd6fe !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-purple-light:hover) {
  background: #ede9fe !important;
  border-color: #c4b5fd !important;
}
:deep(.btn-cyan-light) {
  color: #0891b2 !important;
  background: #ecfeff !important;
  border: 1px solid #a5f3fc !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-cyan-light:hover) {
  background: #cffafe !important;
  border-color: #67e8f9 !important;
}
:deep(.btn-red-light) {
  color: #dc2626 !important;
  background: #fef2f2 !important;
  border: 1px solid #fecaca !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-red-light:hover) {
  background: #fee2e2 !important;
  border-color: #fca5a5 !important;
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
:deep(.btn-orange-light) {
  color: #ea580c !important;
  background: #fff7ed !important;
  border: 1px solid #fed7aa !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-orange-light:hover) {
  background: #ffedd5 !important;
  border-color: #fdba74 !important;
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

.proof-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-filters {
  display: flex;
  gap: 12px;
}

.section-title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.pending-badge {
  background: #ec5b13;
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.proof-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .proof-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .proof-grid { grid-template-columns: 1fr; }
}

.proof-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.proof-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.proof-image {
  position: relative;
  width: 100%;
  height: 180px;
  cursor: pointer;
  overflow: hidden;
}

.proof-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
}

.proof-image:hover .image-overlay {
  opacity: 1;
}

.proof-status-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.proof-status-overlay.pending {
  background: #f59e0b;
}

.proof-status-overlay.approved {
  background: #10b981;
}

.proof-status-overlay.rejected {
  background: #f97316;
}

.proof-content {
  padding: 16px;
}

.proof-id {
  font-size: 12px;
  color: #ec5b13;
  font-weight: 600;
  margin-bottom: 12px;
}

.proof-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.submit-time {
  font-size: 12px;
  color: #9ca3af;
}

.proof-activity-type {
  margin-bottom: 12px;
}

.proof-description {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.proof-actions {
  display: flex;
  gap: 8px;
}

.proof-actions .el-button {
  flex: 1;
}

.proof-pagination {
  display: flex;
  justify-content: center;
}

.preview-content h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-item .label {
  color: #6b7280;
  font-weight: 500;
  min-width: 80px;
}

.preview-item span:last-child {
  color: #1f2937;
}

.preview-description {
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.preview-description h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.preview-description p {
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

:deep(.el-button--primary) {
  background-color: #ec5b13;
  border-color: #ec5b13;
}

:deep(.el-button--primary:hover) {
  background-color: #d14a0a;
  border-color: #d14a0a;
}

:deep(.el-button--success) {
  background-color: #16a34a;
  border-color: #16a34a;
}

:deep(.el-button--success:hover) {
  background-color: #15803d;
  border-color: #15803d;
}

.upload-section {
  width: 100%;
}

.image-uploader {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #ec5b13;
}

.uploaded-image {
  width: 100%;
  height: 120px;
  position: relative;
}

.uploaded-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.uploaded-image:hover .image-actions {
  opacity: 1;
}

.image-actions .el-icon {
  font-size: 24px;
  color: #fff;
  cursor: pointer;
}

.upload-placeholder {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
