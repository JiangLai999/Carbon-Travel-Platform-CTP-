<template>
  <div class="points-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">积分奖励标准设置</h1>
        <p class="page-subtitle">配置不同绿色出行方式的每公里奖励积分，及系统全局有效期设置</p>
      </div>
      <div class="header-right">
        <el-button :icon="Clock" @click="showHistory = true">修改历史</el-button>
        <el-button type="primary" :icon="Check" @click="saveGlobalConfig">保存全局配置</el-button>
      </div>
    </div>

    <div class="global-settings">
      <div class="setting-card">
        <div class="setting-header">
          <el-icon :size="20" color="#ec5b13"><Timer /></el-icon>
          <span class="setting-title">积分有效期设置</span>
        </div>
        <el-select v-model="globalSettings.validityType" style="width: 100%">
          <el-option label="固定年度清零" value="yearly" />
          <el-option label="滚动有效期365天" value="rolling" />
          <el-option label="永久有效" value="permanent" />
        </el-select>
      </div>

      <div class="setting-card">
        <div class="setting-header">
          <el-icon :size="20" color="#ec5b13"><Star /></el-icon>
          <span class="setting-title">全局积分倍率</span>
        </div>
        <div class="input-group">
          <span class="input-label">节假日奖励倍数</span>
          <el-input-number v-model="globalSettings.holidayMultiplier" :min="1" :max="10" :precision="1" :step="0.5" style="width: 100%" />
        </div>
      </div>

      <div class="setting-card">
        <div class="setting-header">
          <el-icon :size="20" color="#ec5b13"><Key /></el-icon>
          <span class="setting-title">防作弊策略</span>
        </div>
        <div class="input-group">
          <span class="input-label">每日获取上限</span>
          <el-input-number v-model="globalSettings.dailyLimit" :min="0" :max="10000" :step="50" style="width: 100%" />
        </div>
        <div class="input-group" style="margin-top: 12px;">
          <span class="input-label">单次最大距离</span>
          <el-input-number v-model="globalSettings.maxDistancePerTrip" :min="1" :max="200" :step="5" style="width: 100%" />
        </div>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索出行方式"
            :prefix-icon="Search"
            clearable
            style="width: 280px"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="showAddDialog = true">添加出行方式</el-button>
      </div>

      <el-table :data="filteredRules" border style="width: 100%">
        <el-table-column label="出行方式" min-width="180">
          <template #default="{ row }">
            <div class="travel-mode">
              <div class="mode-icon" :class="row.iconClass">
                <el-icon :size="20">
                  <component :is="row.icon" />
                </el-icon>
              </div>
              <span class="mode-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="奖励标准" width="160" align="center">
          <template #default="{ row }">
            <div class="reward-input">
              <el-input-number v-model="row.pointsPerKm" :min="0" :max="100" :step="1" size="small" style="width: 100px" />
              <span class="unit">积分/公里</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单次最高限制" width="140" align="center">
          <template #default="{ row }">
            <span class="limit-value">{{ row.maxPerTrip }}积分</span>
          </template>
        </el-table-column>
        <el-table-column label="低碳权重" width="120" align="center">
          <template #default="{ row }">
            <span class="weight-tag" :class="row.weight">
              {{ row.weight === 'high' ? '极高' : '中等' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" active-text="启用中" inactive-text="已停用" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button class="btn-teal-light" text size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button class="btn-cyan-light" text size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="tip-box">
      <el-icon :size="18" color="#ec5b13"><InfoFilled /></el-icon>
      <div class="tip-content">
        <p class="tip-title">修改注意事项</p>
        <p class="tip-text">1. 修改积分规则后，新规则将在次日0点生效，已发放的积分不受影响。</p>
        <p class="tip-text">2. 建议在低峰期进行规则调整，避免影响用户体验。</p>
        <p class="tip-text">3. 所有修改操作将被记录在修改历史中，可随时回溯查看。</p>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" title="添加出行方式" width="500px">
      <el-form :model="newRule" label-width="100px">
        <el-form-item label="出行方式">
          <el-input v-model="newRule.name" placeholder="请输入出行方式名称" />
        </el-form-item>
        <el-form-item label="奖励标准">
          <el-input-number v-model="newRule.pointsPerKm" :min="0" :max="100" :step="1" style="width: 150px" />
          <span style="margin-left: 8px; color: #6b7280;">积分/公里</span>
        </el-form-item>
        <el-form-item label="单次上限">
          <el-input-number v-model="newRule.maxPerTrip" :min="0" :max="1000" :step="10" style="width: 150px" />
          <span style="margin-left: 8px; color: #6b7280;">积分</span>
        </el-form-item>
        <el-form-item label="低碳权重">
          <el-radio-group v-model="newRule.weight">
            <el-radio value="high">极高</el-radio>
            <el-radio value="medium">中等</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图标">
          <el-select v-model="newRule.iconType" style="width: 100%">
            <el-option label="徒步" value="walking" />
            <el-option label="骑行" value="bike" />
            <el-option label="公交" value="bus" />
            <el-option label="地铁" value="metro" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button class="btn-teal-solid" @click="handleAddRule">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑出行方式" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="出行方式">
          <el-input v-model="editForm.name" disabled />
        </el-form-item>
        <el-form-item label="奖励标准">
          <el-input-number v-model="editForm.pointsPerKm" :min="0" :max="100" :step="1" style="width: 150px" />
          <span style="margin-left: 8px; color: #6b7280;">积分/公里</span>
        </el-form-item>
        <el-form-item label="单次上限">
          <el-input-number v-model="editForm.maxPointsPerTrip" :min="0" :max="1000" :step="10" style="width: 150px" />
          <span style="margin-left: 8px; color: #6b7280;">积分/次</span>
        </el-form-item>
        <el-form-item label="碳减排量">
          <el-input-number v-model="editForm.carbonReduction" :min="0" :max="1" :step="0.01" :precision="2" style="width: 150px" />
          <span style="margin-left: 8px; color: #6b7280;">kg/公里</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button class="btn-teal-solid" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showHistory" title="修改历史" width="700px">
      <el-table :data="historyRecords" border style="width: 100%">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="content" label="修改内容" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'info'" size="small">
              {{ row.status === 'success' ? '已生效' : '待生效' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock, Check, Timer, Star, Key, Search, Plus, InfoFilled,
  User, Promotion, Van, Location, Bicycle, Odometer
} from '@element-plus/icons-vue'

const iconComponents = {
  Location,
  Bicycle,
  Van,
  Odometer,
  User,
  Promotion
}
import api from '../../api'

const searchQuery = ref('')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showHistory = ref(false)
const loading = ref(false)

const globalSettings = ref({
  validityType: 'rolling',
  holidayMultiplier: 2.0,
  dailyLimit: 500,
  maxDistancePerTrip: 50
})

const rules = ref([])

const newRule = ref({
  name: '',
  pointsPerKm: 5,
  maxPerTrip: 50,
  weight: 'medium',
  iconType: 'walking'
})

const editForm = ref({
  id: null,
  name: '',
  pointsPerKm: 0,
  maxPointsPerTrip: 100,
  carbonReduction: 0,
  enabled: true
})

const historyRecords = ref([])

const filteredRules = computed(() => {
  if (!searchQuery.value) return rules.value
  const query = searchQuery.value.toLowerCase()
  return rules.value.filter(r => (r.name || r.modeName || '').toLowerCase().includes(query))
})

const loadRules = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/points-rules')
    if (Array.isArray(res)) {
      const iconMap = {
        'location': { icon: 'Location', iconClass: 'walking' },
        'bicycle': { icon: 'Bicycle', iconClass: 'bike' },
        'bus': { icon: 'Van', iconClass: 'bus' },
        'metro': { icon: 'Odometer', iconClass: 'metro' },
        'walking': { icon: 'Location', iconClass: 'walking' },
        'bike': { icon: 'Bicycle', iconClass: 'bike' },
        '骑行': { icon: 'Bicycle', iconClass: 'bike' },
        '公交': { icon: 'Van', iconClass: 'bus' },
        '地铁': { icon: 'Odometer', iconClass: 'metro' },
        '电动车': { icon: 'Odometer', iconClass: 'metro' }
      }
      rules.value = res.map(r => {
        const modeName = r.modeName || r.name || '未知方式'
        const iconName = r.icon || modeName
        const iconInfo = iconMap[iconName] || { icon: 'Location', iconClass: 'walking' }
        return {
          ...r,
          id: r.travelModeId || r.id,
          name: modeName,
          icon: iconComponents[iconInfo.icon] || Location,
          iconClass: iconInfo.iconClass,
          enabled: r.status === 1,
          pointsPerKm: r.pointsPerKm || 0,
          maxPerTrip: r.maxPointsPerTrip || r.maxPerTrip || 100,
          carbonReduction: r.carbonReduction || 0,
          weight: (r.carbonReduction || 0) > 0.1 ? 'high' : 'medium'
        }
      })
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载积分规则失败')
  } finally {
    loading.value = false
  }
}

const saveGlobalConfig = async () => {
  try {
    await api.put('/admin/system-config', {
      validity_type: globalSettings.value.validityType,
      holiday_multiplier: String(globalSettings.value.holidayMultiplier),
      daily_limit: String(globalSettings.value.dailyLimit),
      max_distance_per_trip: String(globalSettings.value.maxDistancePerTrip || 50)
    })
    ElMessage.success('全局配置已保存')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存配置失败')
  }
}

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name,
    pointsPerKm: row.pointsPerKm || 0,
    maxPointsPerTrip: row.maxPointsPerTrip || 100,
    carbonReduction: row.carbonReduction || 0,
    enabled: row.enabled !== false
  }
  showEditDialog.value = true
}

const saveEdit = async () => {
  try {
    await api.put(`/admin/points-rules/${editForm.value.id}`, {
      pointsPerKm: editForm.value.pointsPerKm,
      maxPointsPerTrip: editForm.value.maxPointsPerTrip,
      carbonReduction: editForm.value.carbonReduction,
      enabled: editForm.value.enabled
    })
    ElMessage.success('积分规则已更新')
    showEditDialog.value = false
    loadRules()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认禁用该出行方式？禁用后将不再参与积分计算。', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.put(`/admin/points-rules/${row.id}`, {
        enabled: false
      })
      row.enabled = false
      ElMessage.success('出行方式已禁用')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleAddRule = async () => {
  if (!newRule.value.name) {
    ElMessage.warning('请输入出行方式名称')
    return
  }
  try {
    const iconMap = {
      walking: 'location',
      bike: 'bicycle',
      bus: 'bus',
      metro: 'metro'
    }
    await api.post('/admin/points-rules', {
      name: newRule.value.name,
      pointsPerKm: newRule.value.pointsPerKm,
      carbonReduction: newRule.value.weight === 'high' ? 0.15 : 0.05,
      icon: iconMap[newRule.value.iconType] || 'location',
      sortOrder: 99,
      status: 1
    })
    ElMessage.success('出行方式已添加')
    showAddDialog.value = false
    newRule.value = {
      name: '',
      pointsPerKm: 5,
      maxPerTrip: 50,
      weight: 'medium',
      iconType: 'walking'
    }
    loadRules()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

const loadGlobalConfig = async () => {
  try {
    const res = await api.get('/admin/system-config')
    if (res) {
      globalSettings.value.validityType = res.validity_type || 'rolling'
      globalSettings.value.holidayMultiplier = parseFloat(res.holiday_multiplier) || 2.0
      globalSettings.value.dailyLimit = parseInt(res.daily_limit) || 500
      globalSettings.value.maxDistancePerTrip = parseInt(res.max_distance_per_trip) || 50
    }
  } catch (e) {
    console.error('加载全局配置失败', e)
  }
}

onMounted(() => {
  loadRules()
  loadGlobalConfig()
})
</script>

<style scoped>
.points-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.header-right .el-button--primary {
  background: #ec5b13;
  border-color: #ec5b13;
}

.header-right .el-button--primary:hover {
  background: #d14f0f;
  border-color: #d14f0f;
}

.global-settings {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .global-settings { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .global-settings { grid-template-columns: 1fr; }
}

.setting-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 20px 24px;
}

.setting-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.setting-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 12px;
  color: #6b7280;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  margin-bottom: 24px;
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

.table-header .el-button--primary {
  background: #ec5b13;
  border-color: #ec5b13;
}

.table-header .el-button--primary:hover {
  background: #d14f0f;
  border-color: #d14f0f;
}

.search-bar {
  display: flex;
  gap: 12px;
}

.travel-mode {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mode-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.mode-icon.walking { background: linear-gradient(135deg, #10b981, #059669); }
.mode-icon.bike { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.mode-icon.bus { background: linear-gradient(135deg, #f59e0b, #d97706); }
.mode-icon.metro { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.mode-name {
  font-weight: 600;
  color: #1f2937;
}

.reward-input {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.unit {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
}

.limit-value {
  font-weight: 600;
  color: #ec5b13;
}

.weight-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.weight-tag.high {
  background: #dcfce7;
  color: #166534;
}

.weight-tag.medium {
  background: #fef3c7;
  color: #92400e;
}

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

/* 积分规则 - 青色系浅色按钮 */
:deep(.btn-teal-light) {
  color: #0d9488 !important;
  background: #f0fdfa !important;
  border: 1px solid #99f6e4 !important;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}
:deep(.btn-teal-light:hover) {
  background: #ccfbf1 !important;
  border-color: #5eead4 !important;
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
:deep(.btn-teal-solid) {
  background: #14b8a6 !important;
  border-color: #14b8a6 !important;
  color: #fff !important;
}
:deep(.btn-teal-solid:hover) {
  background: #0d9488 !important;
  border-color: #0d9488 !important;
}

.tip-box {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.tip-text {
  font-size: 13px;
  color: #6b7280;
  margin: 4px 0;
  line-height: 1.6;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #ec5b13;
  border-color: #ec5b13;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: #ec5b13;
}

:deep(.el-input-number__decrease.is-controls),
:deep(.el-input-number__increase.is-controls) {
  border-color: #e5e7eb;
}
</style>
