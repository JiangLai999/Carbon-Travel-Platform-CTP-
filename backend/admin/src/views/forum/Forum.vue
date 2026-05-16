<template>
  <div class="forum-page">
    <div class="page-header">
      <h1 class="page-title">论坛内容审核</h1>
      <p class="page-subtitle">审核、过滤和管理社区讨论内容</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon red">
            <el-icon :size="20"><Warning /></el-icon>
          </div>
          <span class="stat-badge" :class="stats.pendingReports > 0 ? 'danger' : 'success'">{{ stats.pendingReports > 0 ? `${stats.pendingReports}条` : '无' }}</span>
        </div>
        <div class="stat-value">{{ stats.pendingReports }}</div>
        <div class="stat-label">待处理举报</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon blue">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <span class="stat-badge" :class="stats.todayPosts > 0 ? 'success' : 'neutral'">{{ stats.todayPosts > 0 ? `+${stats.todayPosts}` : '暂无' }}</span>
        </div>
        <div class="stat-value">{{ stats.activePosts.toLocaleString() }}</div>
        <div class="stat-label">帖子总数</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon green">
            <el-icon :size="20"><TrendCharts /></el-icon>
          </div>
          <span class="stat-badge success">活跃</span>
        </div>
        <div class="stat-value">{{ stats.activePosts.toLocaleString() }}</div>
        <div class="stat-label">活跃帖子</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon orange">
            <el-icon :size="20"><Timer /></el-icon>
          </div>
          <span class="stat-badge" :class="stats.pendingPosts > 0 ? 'warning' : 'neutral'">{{ stats.pendingPosts > 0 ? `${stats.pendingPosts}条` : '无' }}</span>
        </div>
        <div class="stat-value">{{ stats.pendingPosts }}</div>
        <div class="stat-label">待审核帖子</div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <div class="category-buttons">
          <el-button 
            v-for="cat in categories" 
            :key="cat.value"
            :type="activeCategory === cat.value ? 'primary' : 'default'"
            :class="{ 'active-btn': activeCategory === cat.value }"
            size="small"
            @click="activeCategory = cat.value"
          >
            {{ cat.label }}
          </el-button>
        </div>
        <el-select v-model="statusFilter" placeholder="审核状态" size="small" style="width: 140px" clearable>
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已审核" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已置顶" value="pinned" />
          <el-option label="有举报" value="reported" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button :icon="Refresh" @click="loadPosts">刷新</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="filteredPosts" border style="width: 100%" :row-class-name="getRowClass">
        <el-table-column label="标题与摘要" min-width="280">
          <template #default="{ row }">
            <div class="post-title-cell">
              <div class="post-title">{{ row.title }}</div>
              <div class="post-summary">{{ row.summary }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar">{{ row.author.charAt(0) }}</el-avatar>
              <span class="user-name">{{ row.author }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="互动量" width="120" align="center">
          <template #default="{ row }">
            <div class="interaction-cell">
              <span class="likes"><el-icon><Star /></el-icon> {{ row.likes }}</span>
              <span class="comments"><el-icon><ChatDotRound /></el-icon> {{ row.comments }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button class="btn-purple-light" size="small" @click="viewPostDetail(row)">详情</el-button>
              <!-- 待审核状态显示审核按钮 -->
              <template v-if="row.status === 'pending'">
                <el-button class="btn-green-light" size="small" @click="approvePost(row)">通过</el-button>
                <el-button class="btn-orange-light" size="small" @click="rejectPost(row)">驳回</el-button>
              </template>
              <!-- 已审核状态显示管理按钮 -->
              <template v-else>
                <el-button 
                  class="btn-blue-light"
                  size="small" 
                  @click="togglePin(row)"
                >
                  {{ row.isPinned ? '取消置顶' : '置顶' }}
                </el-button>
                <el-button class="btn-red-light" size="small" @click="deletePost(row)">删除</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="reported-section">
      <div class="section-header">
        <h3>被举报评论概览</h3>
        <span class="report-count">{{ reportedComments.length }} 条待处理</span>
      </div>
      <div class="reported-grid">
        <div v-for="comment in reportedComments" :key="comment.id" class="reported-card">
          <div class="reported-header">
            <div class="user-info">
              <el-avatar :size="36" :src="comment.avatar">{{ comment.user.charAt(0) }}</el-avatar>
              <div class="user-details">
                <div class="user-name">{{ comment.user }}</div>
                <div class="comment-time">{{ comment.time }}</div>
              </div>
            </div>
            <el-tag type="warning" size="small">{{ comment.reportCount }} 条举报</el-tag>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="report-reason">
            <el-icon><Warning /></el-icon>
            <span>举报原因：{{ comment.reason }}</span>
          </div>
            <div class="reported-actions">
              <el-button class="btn-gray-light" size="small" @click="ignoreReport(comment)">忽略</el-button>
              <el-button class="btn-orange-light" size="small" @click="warnUser(comment)">警告</el-button>
              <el-button class="btn-red-light" size="small" @click="removeComment(comment)">移除</el-button>
              <el-button class="btn-blue-light" size="small" @click="reviewCommentPost(comment)">审核帖子</el-button>
            </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewDialogVisible" title="帖子审核" width="600px">
      <div v-if="currentPost" class="review-content">
        <div class="review-item">
          <label>帖子标题</label>
          <div class="review-value">{{ currentPost.title }}</div>
        </div>
        <div class="review-item">
          <label>作者</label>
          <div class="review-value">{{ currentPost.author }}</div>
        </div>
        <div class="review-item">
          <label>分类</label>
          <div class="review-value">{{ currentPost.category }}</div>
        </div>
        <div class="review-item">
          <label>内容摘要</label>
          <div class="review-value">{{ currentPost.summary }}</div>
        </div>
        <div class="review-item">
          <label>互动数据</label>
          <div class="review-value">点赞 {{ currentPost.likes }} · 评论 {{ currentPost.comments }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">关闭</el-button>
        <el-button class="btn-green-solid" @click="approveAndClose">通过</el-button>
        <el-button class="btn-orange-solid" @click="rejectAndClose">驳回</el-button>
        <el-button class="btn-red-solid" @click="deleteAndClose">删除</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="帖子详情" width="700px">
      <div v-if="currentPost" class="detail-content">
        <div class="detail-item">
          <label>帖子标题</label>
          <div class="detail-value title">{{ currentPost.title }}</div>
        </div>
        <div class="detail-item">
          <label>作者</label>
          <div class="detail-value">
            <el-avatar :size="32" :src="currentPost.avatar">{{ currentPost.author ? currentPost.author.charAt(0) : '匿' }}</el-avatar>
            <span>{{ currentPost.author || '匿名用户' }}</span>
          </div>
        </div>
        <div class="detail-item">
          <label>分类</label>
          <div class="detail-value">
            <el-tag :type="getCategoryType(currentPost.category)" size="small">{{ currentPost.category }}</el-tag>
          </div>
        </div>
        <div class="detail-item">
          <label>发布时间</label>
          <div class="detail-value">{{ currentPost.createdAt || currentPost.time }}</div>
        </div>
        <div class="detail-item">
          <label>帖子内容</label>
          <div class="detail-value content">{{ currentPost.content }}</div>
        </div>
        <div class="detail-item" v-if="viewPostImages.length > 0">
          <label>图片附件</label>
          <div class="detail-images">
            <el-image 
              v-for="(img, idx) in viewPostImages" 
              :key="idx"
              :src="getImageUrl(img)"
              :preview-src-list="viewPostImages.map(getImageUrl)"
              fit="cover"
              style="width: 120px; height: 120px; margin-right: 10px; border-radius: 4px;"
            />
          </div>
        </div>
        <div class="detail-item" v-if="currentPost.summary">
          <label>内容摘要</label>
          <div class="detail-value">{{ currentPost.summary }}</div>
        </div>
        <div class="detail-item">
          <label>互动数据</label>
          <div class="detail-value">点赞 {{ currentPost.likes || 0 }} · 评论 {{ currentPost.comments || 0 }}</div>
        </div>
        <div class="detail-item">
          <label>审核状态</label>
          <div class="detail-value">
            <el-tag :type="getStatusType(currentPost)" size="small">{{ getStatusText(currentPost) }}</el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="currentPost.status === 'pending' ? (detailDialogVisible = false, reviewPost(currentPost)) : (detailDialogVisible = false, reviewPost(currentPost))">
          {{ currentPost.status === 'pending' ? '审核此帖' : '重新审核' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Warning, Document, TrendCharts, Timer, Star, ChatDotRound, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const activeCategory = ref('all')
const statusFilter = ref('')
const reviewDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentPost = ref(null)
const loading = ref(false)

const categories = [
  { label: '全部帖子', value: 'all' },
  { label: '经验分享', value: 'experience' },
  { label: '路线攻略', value: 'route' },
  { label: '低碳问答', value: 'qa' }
]

const stats = ref({
  pendingReports: 0,
  pendingPosts: 0,
  activePosts: 0,
  todayPosts: 0,
  totalComments: 0
})

const posts = ref([])

const reportedComments = ref([])

const filteredPosts = computed(() => {
  let result = posts.value
  if (activeCategory.value !== 'all') {
    const categoryMap = {
      experience: '经验分享',
      route: '路线攻略',
      qa: '低碳问答'
    }
    result = result.filter(p => p.category === categoryMap[activeCategory.value])
  }
  if (statusFilter.value) {
    if (statusFilter.value === 'reported') {
      result = result.filter(p => p.reportCount > 0)
    } else if (statusFilter.value === 'pinned') {
      result = result.filter(p => p.isPinned)
    } else if (statusFilter.value === 'hidden') {
      result = result.filter(p => p.isHidden)
    } else if (statusFilter.value === 'pending') {
      result = result.filter(p => p.status === 'pending')
    } else if (statusFilter.value === 'approved') {
      result = result.filter(p => p.status === 'approved' || p.status === 'normal')
    } else if (statusFilter.value === 'rejected') {
      result = result.filter(p => p.status === 'rejected')
    }
  }
  return result
})

const getRowClass = ({ row }) => {
  if (row.reportCount > 0) {
    return 'reported-row'
  }
  return ''
}

const getCategoryType = (category) => {
  const types = {
    '经验分享': 'success',
    '路线攻略': 'primary',
    '低碳问答': 'warning'
  }
  return types[category] || 'info'
}

const getStatusText = (row) => {
  if (row.reportCount > 0) return `${row.reportCount}条举报`
  if (row.isPinned) return '已置顶'
  if (row.status === 'pending') return '待审核'
  if (row.status === 'rejected') return '已驳回'
  return '已审核'
}

const getStatusType = (row) => {
  if (row.status === 'pending') return 'warning'
  if (row.status === 'rejected') return 'warning'
  return 'success'
}

const togglePin = async (row) => {
  try {
    await api.put(`/admin/forum/posts/${row.id}/top`)
    row.isPinned = !row.isPinned
    row.status = row.isPinned ? 'pinned' : 'normal'
    ElMessage.success(row.isPinned ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const toggleHide = async (row) => {
  try {
    await api.put(`/admin/forum/posts/${row.id}/hide`)
    row.isHidden = !row.isHidden
    ElMessage.success(row.isHidden ? '已隐藏' : '已显示')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const deletePost = (row) => {
  ElMessageBox.confirm('确定要删除这篇帖子吗？此操作不可恢复。', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/admin/forum/posts/${row.id}`)
      const index = posts.value.findIndex(p => p.id === row.id)
      if (index > -1) {
        posts.value.splice(index, 1)
      }
      ElMessage.success('帖子已删除')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const viewPostDetail = async (row) => {
  try {
    const postDetail = await api.get(`/admin/forum/posts/${row.id}`)
    currentPost.value = {
      ...row,
      ...(postDetail || {})
    }
    detailDialogVisible.value = true
  } catch (e) {
    currentPost.value = row
    detailDialogVisible.value = true
  }
}

const viewPostImages = computed(() => {
  if (!currentPost.value) return []

  const imagesField = currentPost.value.images ||
    currentPost.value.imageUrls ||
    currentPost.value.imgs ||
    currentPost.value.pictures || ''

  if (!imagesField) return []

  let images = []

  if (Array.isArray(imagesField)) {
    images = imagesField
  } else {
    try {
      const parsed = typeof imagesField === 'string' ? JSON.parse(imagesField) : imagesField
      images = Array.isArray(parsed) ? parsed : (parsed ? [parsed] : [])
    } catch (e) {
      const rawText = String(imagesField).trim()
      images = rawText ? (rawText.includes(',') ? rawText.split(',') : [rawText]) : []
    }
  }

  return images
    .filter(img => !!img)
    .map(img => String(img).trim())
    .filter(img => img.length > 0)
})

const reviewPost = (row) => {
  currentPost.value = row
  reviewDialogVisible.value = true
}

const hideAndClose = async () => {
  if (currentPost.value) {
    try {
      await api.put(`/admin/forum/posts/${currentPost.value.id}/hide`)
      currentPost.value.isHidden = true
      ElMessage.success('帖子已隐藏')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
  reviewDialogVisible.value = false
}

// 快速审核通过
const approvePost = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过该帖子的审核吗？', '审核确认', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'success'
    })
    await api.post(`/admin/forum/posts/${row.id}/review`, null, {
      params: { status: 1 }
    })
    row.status = 'approved'
    ElMessage.success('审核通过')
    loadStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '审核失败')
    }
  }
}

// 快速审核驳回
const rejectPost = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入驳回原因（可选）'
    })
    await api.post(`/admin/forum/posts/${row.id}/review`, null, {
      params: { status: 2, comment: reason || '不符合社区规范' }
    })
    row.status = 'rejected'
    ElMessage.success('已驳回')
    loadStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
}

const deleteAndClose = async () => {
  if (currentPost.value) {
    try {
      await api.delete(`/admin/forum/posts/${currentPost.value.id}`)
      const index = posts.value.findIndex(p => p.id === currentPost.value.id)
      if (index > -1) {
        posts.value.splice(index, 1)
      }
      ElMessage.success('帖子已删除')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
  reviewDialogVisible.value = false
}

const approveAndClose = async () => {
  if (currentPost.value) {
    try {
      await api.post(`/admin/forum/posts/${currentPost.value.id}/review`, null, {
        params: { status: 1 }
      })
      currentPost.value.status = 'approved'
      ElMessage.success('帖子已批准通过')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '审核失败')
    }
  }
  reviewDialogVisible.value = false
}

const rejectAndClose = async () => {
  if (currentPost.value) {
    try {
      const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入驳回原因'
      })
      await api.post(`/admin/forum/posts/${currentPost.value.id}/review`, null, {
        params: { status: 2, comment: reason || '不符合社区规范' }
      })
      currentPost.value.status = 'rejected'
      ElMessage.success('帖子已驳回')
      reviewDialogVisible.value = false
    } catch (e) {
      if (e !== 'cancel') {
        ElMessage.error(e.response?.data?.message || '审核失败')
      }
    }
  }
}

const ignoreReport = async (comment) => {
  try {
    await ElMessageBox.confirm('确认忽略该举报？', '提示', { type: 'info' })
    await api.put(`/admin/forum/comments/${comment.id}/ignore`)
    reportedComments.value = reportedComments.value.filter(c => c.id !== comment.id)
    ElMessage.success('已忽略举报')
    loadReportedComments()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const warnUser = async (comment) => {
  try {
    await ElMessageBox.confirm(`确定要向用户 ${comment.user} 发送警告吗？`, '警告确认', {
      confirmButtonText: '发送警告',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('已发送警告通知')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const removeComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要移除这条评论吗？', '移除确认', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.delete(`/admin/forum/comments/${comment.id}`)
    reportedComments.value = reportedComments.value.filter(c => c.id !== comment.id)
    ElMessage.success('评论已移除')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '移除失败')
  }
}

const loadReportedComments = async () => {
  try {
    const res = await api.get('/admin/forum/comments/reported', { params: { page: 1, size: 50 } })
    if (res && res.records) {
      reportedComments.value = res.records.map(c => ({
        id: c.id,
        postId: c.postId,
        user: c.userName || c.nickname || `用户${c.userId}`,
        avatar: c.avatar || '',
        content: c.content || '',
        time: c.createdAt || '',
        reportCount: c.reportCount || 0,
        reason: c.reportReason || '未知原因'
      }))
    }
  } catch (e) {
    console.error('Failed to load reported comments:', e)
  }
}

const reviewCommentPost = (comment) => {
  const post = posts.value.find(p => p.id === comment.postId)
  if (post) {
    reviewPost(post)
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/forum/posts', { params: { page: 1, size: 50 } })
    if (res && res.records) {
      posts.value = res.records.map(p => {
        // 状态: 0=待审核, 1=已通过, 2=已驳回, -1=已删除
        let status = 'normal'
        if (p.status === 0) status = 'pending'
        else if (p.status === 2) status = 'rejected'
        else if (p.isTop === 1) status = 'pinned'
        else status = 'approved'
        
        return {
          id: p.id,
          title: p.title || '',
          summary: p.content ? p.content.substring(0, 100) + '...' : '',
          author: p.authorName || p.userName || p.nickname || `用户${p.userId}`,
          avatar: p.authorAvatar || p.avatar || '',
          category: getCategoryName(p.sectionId),
          categoryValue: getCategoryValue(p.sectionId),
          likes: p.likes || 0,
          comments: p.commentsCount || 0,
          status: status,
          isPinned: p.isTop === 1,
          isHidden: p.status === 0,
          reportCount: 0,
          rawStatus: p.status
        }
      })
      stats.value.activePosts = res.total || posts.value.length
      stats.value.pendingPosts = posts.value.filter(p => p.status === 'pending').length
    }
  } catch (e) {
    console.error('Failed to load posts:', e)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const getCategoryName = (sectionId) => {
  const map = { 1: '经验分享', 2: '路线攻略', 3: '低碳问答' }
  return map[sectionId] || '其他'
}

const getCategoryValue = (sectionId) => {
  const map = { 1: 'experience', 2: 'route', 3: 'qa' }
  return map[sectionId] || 'other'
}

const getImageUrl = (url) => {
  if (!url) {
    console.log('图片URL为空')
    return ''
  }
  if (url.startsWith('http')) {
    return url
  }
  if (url.startsWith('/uploads')) {
    return '/api' + url
  }
  return '/api/' + url
}

const loadStats = async () => {
  try {
    const res = await api.get('/admin/stats')
    if (res) {
      stats.value = {
        pendingReports: res.pendingReports || 0,
        activePosts: res.totalPosts || 0,
        todayPosts: res.todayPosts || 0,
        totalComments: 0
      }
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

onMounted(() => {
  loadPosts()
  loadStats()
  loadReportedComments()
})
</script>

<style scoped>
.forum-page {
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
  padding: 24px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.blue { background: #dbeafe; color: #2563eb; }
.stat-icon.green { background: #dcfce7; color: #16a34a; }
.stat-icon.orange { background: #ffedd5; color: #ea580c; }
.stat-icon.red,
.stat-icon.warm { background: #fff7ed; color: #ea580c; }

.stat-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}

.stat-badge.success { background: #dcfce7; color: #16a34a; }
.stat-badge.neutral { background: #f3f4f6; color: #4b5563; }
.stat-badge.danger { background: #fff7ed; color: #ea580c; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.category-buttons {
  display: flex;
  gap: 8px;
}

.category-buttons .el-button--primary,
.category-buttons .active-btn {
  background: #ec5b13;
  border-color: #ec5b13;
}

.category-buttons .el-button--primary:hover,
.category-buttons .active-btn:hover {
  background: #d14d0c;
  border-color: #d14d0c;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  margin-bottom: 24px;
}

.table-card :deep(.el-table) {
  --el-table-border-color: #e5e7eb;
}

.table-card :deep(.reported-row) {
  background: #fffaf0 !important;
  border-left: 3px solid #ea580c;
}

.table-card :deep(.reported-row:hover > td) {
  background: #fff7ed !important;
}

.post-title-cell {
  padding: 8px 0;
}

.post-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  line-height: 1.4;
}

.post-summary {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.interaction-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.interaction-cell span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.interaction-cell .likes {
  color: #ec5b13;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.normal {
  background: #dcfce7;
  color: #166534;
}

.status-badge.pinned {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.reported {
  background: #fff7ed;
  color: #ea580c;
}

.status-badge.hidden {
  background: #f3f4f6;
  color: #6b7280;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.normal .status-dot { background: #16a34a; }
.status-badge.pinned .status-dot { background: #2563eb; }
.status-badge.reported .status-dot { background: #ea580c; }
.status-badge.hidden .status-dot { background: #9ca3af; }

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  font-size: 12px;
  padding: 5px 10px;
}

/* 论坛管理 - 紫色系浅色按钮 */
:deep(.btn-purple-light) {
  color: #7c3aed !important;
  background: #f5f3ff !important;
  border: 1px solid #ddd6fe !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-purple-light:hover) {
  background: #ede9fe !important;
  border-color: #c4b5fd !important;
}
:deep(.btn-green-light) {
  color: #059669 !important;
  background: #ecfdf5 !important;
  border: 1px solid #a7f3d0 !important;
  border-radius: 6px;
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
  font-weight: 500;
}
:deep(.btn-orange-light:hover) {
  background: #ffedd5 !important;
  border-color: #fdba74 !important;
}
:deep(.btn-blue-light) {
  color: #2563eb !important;
  background: #eff6ff !important;
  border: 1px solid #bfdbfe !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-blue-light:hover) {
  background: #dbeafe !important;
  border-color: #93c5fd !important;
}
:deep(.btn-red-light) {
  color: #dc2626 !important;
  background: #fef2f2 !important;
  border: 1px solid #fecaca !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-red-light:hover) {
  background: #fee2e2 !important;
  border-color: #fca5a5 !important;
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
:deep(.btn-green-solid) {
  background: #10b981 !important;
  border-color: #10b981 !important;
  color: #fff !important;
}
:deep(.btn-green-solid:hover) {
  background: #059669 !important;
  border-color: #059669 !important;
}
:deep(.btn-orange-solid) {
  background: #f59e0b !important;
  border-color: #f59e0b !important;
  color: #fff !important;
}
:deep(.btn-orange-solid:hover) {
  background: #d97706 !important;
  border-color: #d97706 !important;
}
:deep(.btn-red-solid) {
  background: #ef4444 !important;
  border-color: #ef4444 !important;
  color: #fff !important;
}
:deep(.btn-red-solid:hover) {
  background: #dc2626 !important;
  border-color: #dc2626 !important;
}

.reported-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.report-count {
  font-size: 12px;
  color: #ea580c;
  background: #fff7ed;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.reported-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) {
  .reported-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .reported-grid { grid-template-columns: 1fr; }
}

.reported-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.reported-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.reported-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details .user-name {
  font-weight: 600;
  font-size: 14px;
}

.comment-time {
  font-size: 11px;
  color: #9ca3af;
}

.comment-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  margin-bottom: 12px;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
  border-left: 3px solid #ec5b13;
}

.report-reason {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #ea580c;
  margin-bottom: 12px;
  padding: 8px;
  background: #fffaf0;
  border-radius: 6px;
}

.reported-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.reported-actions .el-button {
  flex: 1;
  min-width: 60px;
  font-size: 12px;
}

.review-content {
  padding: 10px 0;
}

.review-item {
  margin-bottom: 16px;
}

.review-item label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
  font-weight: 600;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-item label {
  display: block;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-item .detail-value {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
}

.detail-item .detail-value.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-item .detail-value.content {
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-value {
  font-size: 14px;
  color: #1f2937;
}

:deep(.el-button--primary) {
  background: #ec5b13;
  border-color: #ec5b13;
}

:deep(.el-button--primary:hover) {
  background: #d14d0c;
  border-color: #d14d0c;
}

:deep(.el-button--primary:focus) {
  background: #ec5b13;
  border-color: #ec5b13;
}
</style>
