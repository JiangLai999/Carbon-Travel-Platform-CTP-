<template>
  <div class="announcement-page">
    <div class="page-header">
      <h1 class="page-title">公告管理</h1>
      <p class="page-subtitle">发布和管理平台公告信息</p>
    </div>

    <div class="table-card">
      <div class="table-header">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索公告标题"
            :prefix-icon="Search"
            clearable
            style="width: 280px"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增公告</el-button>
      </div>

      <el-table :data="announcements" border style="width: 100%">
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="title" label="公告标题" min-width="300"/>
        <el-table-column prop="createdAt" label="发布时间" width="180"/>
        <el-table-column label="操作" width="200" align="right">
          <template #default="{ row }">
            <el-button class="btn-sky-light" size="small" text @click="editAnnouncement(row)">编辑</el-button>
            <el-button class="btn-rose-light" size="small" text @click="deleteAnnouncement(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showAddDialog" :title="isEditing ? '编辑公告' : '新增公告'" width="600px">
      <el-form :model="form">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" placeholder="请输入公告标题"/>
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" rows="5" placeholder="请输入公告内容"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button class="btn-sky-solid" @click="saveAnnouncement">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const searchQuery = ref('')

const announcements = ref([])
const showAddDialog = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const form = ref({ title: '', content: '' })

const openAddDialog = () => {
  isEditing.value = false
  editingId.value = null
  form.value = { title: '', content: '' }
  showAddDialog.value = true
}

onMounted(() => loadAnnouncements())

const loadAnnouncements = async () => {
  try {
    const res = await api.get('/admin/announcements?page=1&size=50')
    announcements.value = res.records || []
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const editAnnouncement = (row) => {
  isEditing.value = true
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    content: row.content || ''
  }
  showAddDialog.value = true
}

const closeDialog = () => {
  showAddDialog.value = false
  isEditing.value = false
  editingId.value = null
  form.value = { title: '', content: '' }
}

const saveAnnouncement = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  try {
    if (isEditing.value) {
      await api.put(`/admin/announcements/${editingId.value}`, form.value)
      ElMessage.success('公告更新成功')
      closeDialog()
      loadAnnouncements()
    } else {
      await api.post('/admin/announcements', form.value)
      ElMessage.success('公告发布成功')
      closeDialog()
      loadAnnouncements()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告"${row.title}"吗？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await api.delete(`/admin/announcements/${row.id}`)
    ElMessage.success('删除成功')
    await nextTick()
    await loadAnnouncements()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.announcement-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.search-bar {
  display: flex;
  gap: 12px;
}

:deep(.el-button--primary) {
  background-color: #ec5b13;
  border-color: #ec5b13;
}

:deep(.el-button--primary:hover) {
  background-color: #d14d0b;
  border-color: #d14d0b;
}

/* 公告管理 - 天蓝色系浅色按钮 */
:deep(.btn-sky-light) {
  color: #0284c7 !important;
  background: #f0f9ff !important;
  border: 1px solid #bae6fd !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-sky-light:hover) {
  background: #e0f2fe !important;
  border-color: #7dd3fc !important;
}
:deep(.btn-rose-light) {
  color: #e11d48 !important;
  background: #fff1f2 !important;
  border: 1px solid #fecdd3 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-rose-light:hover) {
  background: #ffe4e6 !important;
  border-color: #fda4af !important;
}
:deep(.btn-sky-solid) {
  background: #0ea5e9 !important;
  border-color: #0ea5e9 !important;
  color: #fff !important;
}
:deep(.btn-sky-solid:hover) {
  background: #0284c7 !important;
  border-color: #0284c7 !important;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f9fafb;
  --el-table-header-text-color: #6b7280;
}

:deep(.el-table th.el-table__cell) {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: #fff7ed !important;
}
</style>
