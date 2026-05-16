<template>
  <div class="travel-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">出行记录审核</h1>
        <p class="page-subtitle">审核并核实用户提交的减碳数据</p>
      </div>
      <div class="header-actions">
        <el-button @click="exportCSV">
          <el-icon><Download /></el-icon>
          导出CSV
        </el-button>
        <el-button class="btn-emerald-light primary-btn" @click="loadRecords">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-row">
        <div class="status-tabs">
          <div 
            v-for="tab in statusTabs" 
            :key="tab.value" 
            class="status-tab"
            :class="{ active: activeTab === tab.value }"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
            <span class="tab-count">{{ tab.count }}</span>
          </div>
        </div>
      </div>

      <div class="filter-row second-row">
        <div class="mode-filter">
          <span class="filter-label">出行方式:</span>
          <div class="mode-buttons">
            <button 
              v-for="mode in travelModes" 
              :key="mode.value"
              class="mode-btn"
              :class="{ active: activeMode === mode.value }"
              @click="activeMode = mode.value"
            >
              <el-icon><component :is="mode.icon" /></el-icon>
              {{ mode.label }}
            </button>
          </div>
        </div>

        <div class="date-filter">
          <span class="filter-label">日期范围:</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            size="default"
          />
        </div>
      </div>
    </div>

    <div class="table-section">
      <el-table :data="filteredRecords" border style="width: 100%" v-loading="loading" @row-click="openDetail">
        <el-table-column label="用户" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar || ''">{{ (row.userName || 'U').charAt(0).toUpperCase() }}</el-avatar>
              <div class="user-info">
                <span class="user-name" :title="row.userName">{{ row.userName }}</span>
                <span class="user-id">ID: {{ row.userId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="出行方式" width="90" align="center">
          <template #default="{ row }">
            <div class="mode-cell">
              <el-icon :size="18" :class="'mode-icon ' + row.mode">
                <component :is="getModeIcon(row.mode)" />
              </el-icon>
              <span class="mode-text">{{ row.modeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="起点-终点" min-width="140">
          <template #default="{ row }">
            <div class="route-cell">
              <span class="route-start" :title="row.startLocation">{{ row.startLocation || '起点' }}</span>
              <span class="route-arrow">→</span>
              <span class="route-end" :title="row.endLocation">{{ row.endLocation || '终点' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="距离" width="70" align="center">
          <template #default="{ row }">
            <span class="distance-value">{{ row.distance }} km</span>
          </template>
        </el-table-column>
        <el-table-column label="碳减排" width="70" align="center">
          <template #default="{ row }">
            <span class="carbon-value">{{ row.carbonReduction }} kg</span>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="70" align="center">
          <template #default="{ row }">
            <span class="points-value">+{{ row.pointsEarned || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="130" align="center">
          <template #default="{ row }">
            <span class="time-value">{{ formatTime(row.submitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status">
              <span class="status-dot"></span>
              {{ getStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <template v-if="row.status === 'pending'">
                <el-button class="btn-green-light" size="small" plain @click.stop="approve(row)">通过</el-button>
                <el-button class="btn-gray-light" size="small" plain @click.stop="reject(row)">驳回</el-button>
              </template>
              <template v-else>
                <el-button class="btn-emerald-light" size="small" text @click.stop="openDetail(row)">查看详情</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @change="loadRecords"
        />
      </div>
    </div>

    <el-dialog 
      v-model="showDetail" 
      title="审核详情"
      width="700px"
      class="detail-dialog"
      :close-on-click-modal="false"
    >
      <div class="detail-content" v-if="currentRecord">
        <div class="detail-header">
          <div class="record-status" :class="currentRecord.status">
            <span class="status-dot"></span>
            {{ getStatusLabel(currentRecord.status) }}
          </div>
          <div class="record-id">记录ID: {{ currentRecord.id }}</div>
        </div>
        
        <div class="detail-grid">
          <div class="detail-section">
            <h4 class="section-title">用户信息</h4>
            <div class="user-detail">
              <el-avatar :size="56" :src="currentRecord.avatar || ''">{{ (currentRecord.userName || 'U').charAt(0).toUpperCase() }}</el-avatar>
              <div class="user-meta">
                <div class="user-name">{{ currentRecord.userName }}</div>
                <div class="user-id">用户ID: {{ currentRecord.userId }}</div>
                <div class="user-phone" v-if="currentRecord.phone">手机: {{ currentRecord.phone }}</div>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="section-title">出行信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>出行方式</label>
                <span><el-icon :size="16"><component :is="getModeIcon(currentRecord.mode)" /></el-icon> {{ currentRecord.modeName || getModeLabel(currentRecord.mode) }}</span>
              </div>
              <div class="info-item">
                <label>行程距离</label>
                <span class="highlight">{{ currentRecord.distance }} km</span>
              </div>
              <div class="info-item">
                <label>碳减排量</label>
                <span class="highlight green">{{ currentRecord.carbonReduction }} kg</span>
              </div>
              <div class="info-item">
                <label>预计积分</label>
                <span class="highlight orange">{{ currentRecord.pointsEarned || 0 }} 积分</span>
              </div>
            </div>
          </div>

          <div class="detail-section full-width">
            <h4 class="section-title">路线信息</h4>
            <div class="route-info">
              <div class="route-point">
                <div class="point-marker start"></div>
                <div class="point-content">
                  <span class="point-label">起点</span>
                  <span class="point-value">{{ currentRecord.startLocation || '未知' }}</span>
                </div>
              </div>
              <div class="route-line"></div>
              <div class="route-point">
                <div class="point-marker end"></div>
                <div class="point-content">
                  <span class="point-label">终点</span>
                  <span class="point-value">{{ currentRecord.endLocation || '未知' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="section-title">提交时间</h4>
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(currentRecord.submitTime) }}</span>
            </div>
          </div>

          <div class="detail-section" v-if="currentRecord.reviewComment">
            <h4 class="section-title">审核意见</h4>
            <div class="review-info" :class="currentRecord.status">
              {{ currentRecord.reviewComment }}
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer" v-if="currentRecord && currentRecord.status === 'pending'">
          <el-button @click="showDetail = false">取消</el-button>
          <el-button class="btn-gray-light" @click="rejectFromDetail">驳回</el-button>
          <el-button class="btn-green-solid primary-btn" @click="approveFromDetail">通过审核</el-button>
        </div>
        <div class="dialog-footer" v-else>
          <el-button @click="showDetail = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { 
  Download, Refresh, Location, Clock, Picture, Cpu,
  Van, Bicycle, Position
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const activeTab = ref('all')
const activeMode = ref('all')
const dateRange = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showDetail = ref(false)
const currentRecord = ref(null)
const loading = ref(false)
const operatingId = ref(null)

const statusTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待审核', value: 'pending', count: 0 },
  { label: '已通过', value: 'approved', count: 0 },
  { label: '已驳回', value: 'rejected', count: 0 }
])

const travelModes = [
  { label: '全部', value: 'all', icon: Position },
  { label: '步行', value: 'walk', icon: Position },
  { label: '骑行', value: 'bike', icon: Bicycle },
  { label: '公交', value: 'bus', icon: Van },
  { label: '地铁', value: 'metro', icon: Van }
]

const records = ref([])

const filteredRecords = computed(() => {
  return records.value
})

const getModeIcon = (mode) => {
  const icons = { 
    '步行': Position, '步行/跑步': Position, 'walk': Position, 'walking': Position,
    '骑行': Bicycle, '自行车': Bicycle, 'bike': Bicycle, 'cycling': Bicycle,
    '公交': Van, '公交车': Van, 'bus': Van, 'transit': Van,
    '地铁': Van, 'metro': Van, 'subway': Van,
    '电动车': Van
  }
  return icons[mode] || Position
}

const getModeLabel = (mode) => {
  if (!mode) return '未知'
  const labels = { 
    '步行': '步行', '步行/跑步': '步行', 'walk': '步行', 'walking': '步行',
    '骑行': '骑行', '自行车': '骑行', 'bike': '骑行', 'cycling': '骑行',
    '公交': '公交', '公交车': '公交', 'bus': '公交', 'transit': '公交',
    '地铁': '地铁', 'metro': '地铁', 'subway': '地铁',
    '电动车': '电动车'
  }
  return labels[mode] || mode
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

const getStatusLabel = (status) => {
  const labels = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return labels[status] || status || '待审核'
}

// Map backend status int to string
const mapStatus = (s) => {
  if (s === 0) return 'pending'
  if (s === 1) return 'approved'
  if (s === 2) return 'rejected'
  return s
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (activeTab.value !== 'all') {
      const statusMap = { pending: 0, approved: 1, rejected: 2 }
      params.status = statusMap[activeTab.value]
    }
    if (activeMode.value !== 'all') {
      const modeMap = { walk: 1, bike: 2, bus: 3, metro: 4 }
      params.travelModeId = modeMap[activeMode.value]
    }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const [recordsRes, statsRes] = await Promise.all([
      api.get('/travel/pending', { params }),
      api.get('/travel/stats')
    ])
    
    if (recordsRes.records) {
      records.value = recordsRes.records.map(r => {
        const travelMode = r.travelMode || r.travelModeName || '未知'
        return {
          ...r,
          mode: travelMode,
          modeName: getModeLabel(travelMode),
          status: mapStatus(r.status),
          submitTime: r.createdAt || '',
          startLocation: r.startLocation || '-',
          endLocation: r.endLocation || '-',
          carbonReduction: r.carbonReduction || 0,
          pointsEarned: r.pointsEarned || 0
        }
      })
      total.value = recordsRes.total || 0
    }
    
    if (statsRes) {
      statusTabs.value[0].count = statsRes.total || 0
      statusTabs.value[1].count = statsRes.pending || 0
      statusTabs.value[2].count = statsRes.approved || 0
      statusTabs.value[3].count = statsRes.rejected || 0
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

const exportCSV = () => {
  const headers = ['用户ID', '出行方式', '距离(km)', '减排CO2(kg)', '提交时间', '状态']
  const rows = filteredRecords.value.map(r => [
    r.userId, getModeLabel(r.mode), r.distance, r.carbonReduction, r.submitTime, getStatusLabel(r.status)
  ])
  const csv = [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `出行记录_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const openDetail = (row) => {
  currentRecord.value = row
  showDetail.value = true
}

const approve = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该出行记录审核?', '审核确认', { type: 'success' })
    operatingId.value = row.id
    await api.post(`/travel/review/${row.id}?status=1&comment=审核通过`)
    ElMessage.success('审核通过')
    loadRecords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  } finally {
    operatingId.value = null
  }
}

const reject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回确认', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入驳回原因',
      inputValue: '信息不符'
    })
    operatingId.value = row.id
    await api.post(`/travel/review/${row.id}?status=2&comment=${encodeURIComponent(reason || '不符合要求')}`)
    ElMessage.success('已驳回')
    loadRecords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  } finally {
    operatingId.value = null
  }
}

const approveFromDetail = async () => {
  if (currentRecord.value) {
    await approve(currentRecord.value)
    showDetail.value = false
  }
}

const rejectFromDetail = async () => {
  if (currentRecord.value) {
    await reject(currentRecord.value)
    showDetail.value = false
  }
}

onMounted(() => loadRecords())

watch([activeTab, activeMode, dateRange], () => {
  page.value = 1
  loadRecords()
})

watch([page, size], () => {
  loadRecords()
})
</script>

<style scoped>
.travel-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.primary-btn {
  background: #ec5b13 !important;
  border-color: #ec5b13 !important;
}

.primary-btn:hover {
  background: #d14f0f !important;
  border-color: #d14f0f !important;
}

/* 出行审核 - 翠绿色系浅色按钮 */
:deep(.btn-emerald-light) {
  color: #059669 !important;
  background: #ecfdf5 !important;
  border: 1px solid #a7f3d0 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-emerald-light:hover) {
  background: #d1fae5 !important;
  border-color: #6ee7b7 !important;
}
:deep(.btn-green-light) {
  color: #16a34a !important;
  background: #f0fdf4 !important;
  border: 1px solid #bbf7d0 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-green-light:hover) {
  background: #dcfce7 !important;
  border-color: #86efac !important;
}
:deep(.btn-green-solid) {
  background: #16a34a !important;
  border-color: #16a34a !important;
  color: #fff !important;
}
:deep(.btn-green-solid:hover) {
  background: #15803d !important;
  border-color: #15803d !important;
}
:deep(.btn-gray-light) {
  color: #4b5563 !important;
  background: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-gray-light:hover) {
  background: #f3f4f6 !important;
  border-color: #d1d5db !important;
}

.filter-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 20px 24px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  align-items: center;
}

.filter-row.second-row {
  margin-top: 16px;
  gap: 32px;
}

.status-tabs {
  display: flex;
  gap: 8px;
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f9fafb;
}

.status-tab:hover {
  background: #f3f4f6;
  color: #374151;
}

.status-tab.active {
  background: #ec5b13;
  color: #fff;
}

.tab-count {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.2);
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  margin-right: 12px;
}

.mode-filter {
  display: flex;
  align-items: center;
}

.mode-buttons {
  display: flex;
  gap: 8px;
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-btn:hover {
  border-color: #ec5b13;
  color: #ec5b13;
}

.mode-btn.active {
  background: #fff7ed;
  border-color: #ec5b13;
  color: #ec5b13;
}

.date-filter {
  display: flex;
  align-items: center;
}

.table-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.pagination-wrapper {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e7eb;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.user-info .user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info .user-id {
  font-size: 11px;
  color: #9ca3af;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.mode-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.mode-text {
  font-size: 12px;
  color: #6b7280;
}

.mode-icon {
  font-size: 18px;
}

.mode-icon.walk, .mode-icon.walking { color: #10b981; }
.mode-icon.bike, .mode-icon.cycling { color: #3b82f6; }
.mode-icon.bus, .mode-icon.transit { color: #f59e0b; }
.mode-icon.subway, .mode-icon.metro { color: #8b5cf6; }

.distance-value {
  font-weight: 500;
  color: #1f2937;
  font-size: 13px;
  white-space: nowrap;
}

.carbon-value {
  font-weight: 600;
  color: #16a34a;
  font-size: 13px;
  white-space: nowrap;
}

.points-value {
  font-weight: 600;
  color: #ec5b13;
  font-size: 13px;
  white-space: nowrap;
}

.time-value {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.approved {
  background: #dcfce7;
  color: #166534;
}

.status-badge.rejected {
  background: #f3f4f6;
  color: #6b7280;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.pending .status-dot { background: #f59e0b; }
.status-badge.approved .status-dot { background: #16a34a; }
.status-badge.rejected .status-dot { background: #9ca3af; }

.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.detail-content {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.record-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.record-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.record-status.approved {
  background: #dcfce7;
  color: #166534;
}

.record-status.rejected {
  background: #f3f4f6;
  color: #6b7280;
}

.record-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.record-status.pending .status-dot { background: #f59e0b; }
.record-status.approved .status-dot { background: #16a34a; }
.record-status.rejected .status-dot { background: #9ca3af; }

.record-id {
  font-size: 14px;
  color: #6b7280;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-section {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
}

.detail-section.full-width {
  grid-column: 1 / -1;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 12px;
}

.user-detail {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-meta .user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.user-meta .user-id {
  font-size: 13px;
  color: #6b7280;
}

.user-meta .user-phone {
  font-size: 12px;
  color: #9ca3af;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  color: #6b7280;
}

.info-item span {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-item .highlight {
  font-size: 16px;
  font-weight: 700;
}

.info-item .highlight.green {
  color: #16a34a;
}

.info-item .highlight.orange {
  color: #ec5b13;
}

.route-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-point {
  display: flex;
  align-items: center;
  gap: 12px;
}

.point-marker {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.point-marker.start {
  background: #ec5b13;
}

.point-marker.end {
  background: #16a34a;
}

.point-content {
  display: flex;
  flex-direction: column;
}

.point-content .point-label {
  font-size: 11px;
  color: #9ca3af;
}

.point-content .point-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1f2937;
}

.review-info {
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #4b5563;
}

.review-info.approved {
  background: #dcfce7;
}

.review-info.rejected {
  background: #fff7ed;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
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
  cursor: pointer;
}

.route-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.route-start {
  color: #1f2937;
  max-width: 55px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.route-arrow {
  color: #9ca3af;
  flex-shrink: 0;
}

.route-end {
  color: #1f2937;
  max-width: 55px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
</style>
