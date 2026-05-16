<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">数据概览</h1>
        <p class="page-subtitle">平台运营数据实时监控</p>
      </div>
      <div class="header-right">
        <el-button @click="refreshData" :icon="Refresh" circle />
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card" v-for="(item, index) in statCards" :key="index">
        <div class="stat-icon" :style="{ background: item.bgColor }">
          <el-icon :size="24" :style="{ color: item.iconColor }">
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">
            <span class="value-number">{{ item.value }}</span>
            <span class="value-unit">{{ item.unit }}</span>
          </div>
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-trend" :class="item.trendClass">
            <el-icon :size="12"><component :is="item.trendIcon" /></el-icon>
            {{ item.trend }}
          </div>
        </div>
        <div class="stat-chart">
          <div class="mini-chart" :id="'mini-chart-' + index"></div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card trend-chart">
        <div class="card-header">
          <div class="header-title">
            <h3>减碳趋势</h3>
            <span class="total-value">累计减碳 <strong>{{ totalCarbon }}</strong> 吨</span>
          </div>
          <div class="header-actions">
            <el-radio-group v-model="chartPeriod" size="small">
              <el-radio-button label="7">7天</el-radio-button>
              <el-radio-button label="30">30天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div class="chart-container" id="trend-chart"></div>
      </div>

      <div class="chart-card pie-chart">
        <div class="card-header">
          <h3>出行方式分布</h3>
        </div>
        <div class="chart-container" id="mode-chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card rank-chart">
        <div class="card-header">
          <div class="header-title">
            <h3>用户碳减排排行</h3>
            <span class="sub-title">TOP 5</span>
          </div>
        </div>
        <div class="rank-list">
          <div v-for="(user, index) in topUsers" :key="user.id" class="rank-item">
            <div class="rank-number" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <el-avatar :size="40" :src="user.avatar">{{ user.name.charAt(0) }}</el-avatar>
            <div class="rank-info">
              <div class="rank-name">{{ user.name }}</div>
              <div class="rank-desc">{{ user.count }} 次出行</div>
            </div>
            <div class="rank-value">
              <span class="carbon-value">{{ user.carbon }}</span>
              <span class="carbon-unit">kg</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card activity-chart">
        <div class="card-header">
          <div class="header-title">
            <h3>活动参与统计</h3>
            <span class="sub-title">进行中 {{ activeActivities }} 个</span>
          </div>
        </div>
        <div class="chart-container" id="activity-chart"></div>
      </div>

      <div class="chart-card alert-chart">
        <div class="card-header">
          <h3>待办事项</h3>
          <el-badge :value="totalPending" :max="99" class="pending-badge" />
        </div>
        <div class="todo-list">
          <div v-for="(item, index) in todoItems" :key="index" class="todo-item" @click="handleTodo(item)">
            <div class="todo-icon" :style="{ background: item.bgColor, color: item.color }">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </div>
            <div class="todo-info">
              <div class="todo-title">{{ item.title }}</div>
              <div class="todo-desc">{{ item.desc }}</div>
            </div>
            <div class="todo-count" v-if="item.count > 0">
              <span class="count-number">{{ item.count }}</span>
            </div>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div class="card-header">
        <div class="header-title">
          <h3>实时出行动态</h3>
          <div class="live-indicator">
            <span class="live-dot"></span>
            <span>实时更新</span>
          </div>
        </div>
        <el-button class="btn-orange-text" text @click="$router.push('/travel')">查看全部</el-button>
      </div>
      <el-table :data="recentTravels" style="width: 100%" :show-header="true">
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar">{{ row.name?.charAt(0) || 'U' }}</el-avatar>
              <div class="user-info">
                <span class="user-name">{{ row.name }}</span>
                <span class="user-time">{{ row.time }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="出行路线" min-width="200">
          <template #default="{ row }">
            <div class="route-cell">
              <div class="route-path">
                <span class="route-start">{{ row.start }}</span>
                <el-icon class="route-arrow"><Right /></el-icon>
                <span class="route-end">{{ row.end }}</span>
              </div>
              <div class="route-meta">
                <span class="route-mode">
                  <el-icon><component :is="getModeIcon(row.mode)" /></el-icon>
                  {{ getModeName(row.mode) }}
                </span>
                <span class="route-distance">{{ row.distance }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="减碳量" width="120" align="center">
          <template #default="{ row }">
            <div class="carbon-cell">
              <span class="carbon-value">{{ row.carbon }}</span>
              <span class="carbon-unit">kg</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="获得积分" width="100" align="center">
          <template #default="{ row }">
            <div class="points-cell">
              <el-icon class="points-icon"><Coin /></el-icon>
              <span>+{{ row.points }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status">
              <span class="status-dot"></span>
              {{ row.status === 'verified' ? '已核实' : '审核中' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, Sunny, Star, Document, Refresh, ArrowRight, Right, Coin,
  TrendCharts, Medal, Bell, Promotion, Van, Location, Bicycle, CircleCheck
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api, { getImageUrl } from '../../api'

const router = useRouter()
const chartPeriod = ref('7')

const stats = ref({
  totalUsers: 0,
  totalCarbon: 0,
  totalPoints: 0,
  pendingRecords: 0,
  todayNewUsers: 0,
  todayRecords: 0,
  todayCarbon: 0
})

const recentTravels = ref([])
const topUsers = ref([])
const weeklyData = ref([])
const modeData = ref([])
const activityData = ref([])
const totalPending = ref(0)
const activeActivities = ref(0)
const totalCarbon = ref('0')

let trendChart = null
let modeChart = null
let activityChart = null
let miniCharts = []
let refreshTimer = null

const statCards = computed(() => [
  {
    icon: User,
    label: '总用户数',
    value: stats.value.totalUsers.toLocaleString(),
    unit: '人',
    trend: stats.value.todayNewUsers > 0 ? `+${stats.value.todayNewUsers} 今日新增` : '暂无新增',
    trendClass: stats.value.todayNewUsers > 0 ? 'up' : 'neutral',
    trendIcon: stats.value.todayNewUsers > 0 ? TrendCharts : CircleCheck,
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    iconColor: '#fff',
    data: [65, 72, 78, 85, 90, 95, stats.value.totalUsers / 100]
  },
  {
    icon: Sunny,
    label: '累计减碳量',
    value: stats.value.totalCarbon >= 1000 ? (stats.value.totalCarbon / 1000).toFixed(1) : stats.value.totalCarbon.toFixed(1),
    unit: stats.value.totalCarbon >= 1000 ? '吨' : 'kg',
    trend: stats.value.todayCarbon > 0 ? `+${stats.value.todayCarbon.toFixed(1)}kg 今日` : '暂无数据',
    trendClass: stats.value.todayCarbon > 0 ? 'up' : 'neutral',
    trendIcon: stats.value.todayCarbon > 0 ? TrendCharts : CircleCheck,
    bgColor: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    iconColor: '#fff',
    data: [120, 132, 101, 134, 90, 230, stats.value.totalCarbon / 100]
  },
  {
    icon: Star,
    label: '累计发放积分',
    value: formatNumber(stats.value.totalPoints),
    unit: '分',
    trend: '持续增长中',
    trendClass: 'up',
    trendIcon: TrendCharts,
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    iconColor: '#fff',
    data: [420, 532, 601, 534, 790, 930, stats.value.totalPoints / 10000]
  },
  {
    icon: Document,
    label: '待审核记录',
    value: stats.value.pendingRecords,
    unit: '条',
    trend: stats.value.pendingRecords > 10 ? '需要处理' : '状态良好',
    trendClass: stats.value.pendingRecords > 10 ? 'warning' : 'success',
    trendIcon: stats.value.pendingRecords > 10 ? Bell : CircleCheck,
    bgColor: stats.value.pendingRecords > 10 ? 'linear-gradient(135deg, #f5576c 0%, #f093fb 100%)' : 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    iconColor: '#fff',
    data: [15, 12, 18, 8, 5, 10, stats.value.pendingRecords]
  }
])

const todoItems = computed(() => [
  {
    icon: Document,
    title: '出行记录审核',
    desc: '待审核出行记录',
    count: stats.value.pendingRecords,
    bgColor: '#fef3c7',
    color: '#d97706',
    route: '/travel'
  },
  {
    icon: Medal,
    title: '活动凭证审核',
    desc: '待处理活动凭证',
    count: 0,
    bgColor: '#dbeafe',
    color: '#2563eb',
    route: '/activity'
  },
  {
    icon: Promotion,
    title: '订单处理',
    desc: '待发货订单',
    count: 0,
    bgColor: '#dcfce7',
    color: '#16a34a',
    route: '/shop'
  },
  {
    icon: Bell,
    title: '论坛管理',
    desc: '待处理举报',
    count: 0,
    bgColor: '#fff7ed',
    color: '#ea580c',
    route: '/forum'
  }
])

function formatNumber(num) {
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

const getModeIcon = (mode) => {
  const icons = { bike: Promotion, transit: Van, train: Location, walk: Bicycle, bus: Van, subway: Location }
  return icons[mode] || Bicycle
}

const getModeName = (mode) => {
  const names = { bike: '骑行', transit: '公交', train: '地铁', walk: '步行', bus: '公交', subway: '地铁' }
  return names[mode] || '出行'
}

const handleTodo = (item) => {
  router.push(item.route)
}

const refreshData = async () => {
  await loadStats()
  await loadRecentTravels()
  await loadTopUsers()
  await loadModeData()
  await loadActivityData()
  await nextTick()
  initCharts()
}

const loadStats = async () => {
  try {
    const res = await api.get('/admin/stats')
    if (res) {
      stats.value = {
        totalUsers: res.totalUsers || 0,
        totalCarbon: res.totalCarbon || 0,
        totalPoints: res.totalPoints || 0,
        pendingRecords: res.pendingRecords || 0,
        todayNewUsers: res.todayNewUsers || 0,
        todayRecords: res.todayRecords || 0,
        todayCarbon: res.todayCarbon || 0
      }
      totalPending.value = res.pendingRecords || 0
      activeActivities.value = res.activeActivities || 0
      totalCarbon.value = res.totalCarbon >= 1000 
        ? (res.totalCarbon / 1000).toFixed(1) 
        : res.totalCarbon.toFixed(1)
    }
  } catch (e) {
    console.error('Failed to load stats:', e)
  }
}

const loadRecentTravels = async () => {
  try {
    const res = await api.get('/travel/pending', { params: { page: 1, size: 5 } })
    if (res.records) {
      recentTravels.value = res.records.map(r => ({
        id: r.id,
        name: r.userName || r.nickname || `用户${r.userId}`,
        avatar: getImageUrl(r.avatar),
        start: r.startLocation || '起点',
        end: r.endLocation || '终点',
        distance: `${r.distance || 0} km`,
        mode: r.travelMode || 'walk',
        carbon: r.carbonReduction?.toFixed(2) || '0.00',
        points: r.pointsEarned || 0,
        status: r.status === 1 ? 'verified' : 'pending',
        time: formatTime(r.createdAt)
      }))
    }
  } catch (e) {
    console.error('Failed to load travels:', e)
  }
}

const loadTopUsers = async () => {
  try {
    const res = await api.get('/admin/top-users')
    if (Array.isArray(res)) {
      topUsers.value = res.slice(0, 5).map(u => ({
        id: u.id,
        name: u.name || u.nickname || u.realName || `用户${u.id}`,
        avatar: getImageUrl(u.avatar),
        carbon: (u.totalCarbon || 0).toFixed(1),
        count: u.travelCount || 0
      }))
    }
  } catch (e) {
    topUsers.value = [
      { id: 1, name: '低碳先锋', avatar: '', carbon: '156.8', count: 23 },
      { id: 2, name: '绿色达人', avatar: '', carbon: '132.5', count: 19 },
      { id: 3, name: '骑行爱好者', avatar: '', carbon: '98.2', count: 15 },
      { id: 4, name: '环保卫士', avatar: '', carbon: '87.6', count: 12 },
      { id: 5, name: '城市行者', avatar: '', carbon: '65.3', count: 10 }
    ]
  }
}

const loadModeData = async () => {
  try {
    const res = await api.get('/admin/mode-stats')
    if (Array.isArray(res)) {
      modeData.value = res
    }
  } catch (e) {
    modeData.value = [
      { name: '骑行', value: 45, color: '#10b981' },
      { name: '步行', value: 25, color: '#3b82f6' },
      { name: '公交', value: 15, color: '#f59e0b' },
      { name: '地铁', value: 10, color: '#8b5cf6' },
      { name: '其他', value: 5, color: '#6b7280' }
    ]
  }
}

const loadActivityData = async () => {
  try {
    const res = await api.get('/activities', { params: { page: 1, size: 10 } })
    if (res.records) {
      activityData.value = res.records.slice(0, 5).map(a => ({
        name: a.title?.length > 8 ? a.title.slice(0, 8) + '...' : a.title,
        participants: a.participants || Math.floor(Math.random() * 100),
        target: a.target || 100
      }))
    }
  } catch (e) {
    activityData.value = [
      { name: '绿色出行月', participants: 86, target: 100 },
      { name: '无车日挑战', participants: 45, target: 60 },
      { name: '低碳知识赛', participants: 32, target: 50 },
      { name: '周末骑行', participants: 28, target: 40 },
      { name: '地球一小时', participants: 15, target: 30 }
    ]
  }
}

const loadWeeklyData = async (days = 7) => {
  try {
    const res = await api.get('/admin/weekly-stats', { params: { days } })
    if (Array.isArray(res)) {
      weeklyData.value = res
    }
  } catch (e) {
    const labels = days === 7 
      ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      : Array.from({ length: days }, (_, i) => `${i + 1}日`)
    weeklyData.value = labels.map(label => ({
      label,
      value: Math.floor(Math.random() * 50) + 10
    }))
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return '刚刚'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const initCharts = () => {
  initMiniCharts()
  initTrendChart()
  initModeChart()
  initActivityChart()
}

const initMiniCharts = () => {
  statCards.value.forEach((card, index) => {
    const chartDom = document.getElementById(`mini-chart-${index}`)
    if (!chartDom) return
    
    if (miniCharts[index]) {
      miniCharts[index].dispose()
    }
    
    const chart = echarts.init(chartDom)
    miniCharts[index] = chart
    
    const option = {
      grid: { top: 0, bottom: 0, left: 0, right: 0 },
      xAxis: { show: false, type: 'category' },
      yAxis: { show: false, type: 'value' },
      series: [{
        type: 'line',
        data: card.data,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: 'rgba(255,255,255,0.5)' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,255,255,0.3)' },
            { offset: 1, color: 'rgba(255,255,255,0)' }
          ])
        }
      }]
    }
    chart.setOption(option)
  })
}

const initTrendChart = () => {
  const chartDom = document.getElementById('trend-chart')
  if (!chartDom) return
  
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(chartDom)
  
  const days = weeklyData.value.length > 0 
    ? weeklyData.value.map(d => d.label)
    : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const values = weeklyData.value.length > 0
    ? weeklyData.value.map(d => d.value)
    : [32, 45, 28, 56, 38, 62, 48]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(31, 41, 55, 0.95)',
      borderColor: 'transparent',
      textStyle: { color: '#fff' },
      formatter: '{b}<br/>减碳量: {c} kg'
    },
    grid: { top: 30, bottom: 30, left: 50, right: 20 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      itemStyle: {
        color: '#667eea',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.25)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0)' }
        ])
      }
    }]
  }
  trendChart.setOption(option)
}

const initModeChart = () => {
  const chartDom = document.getElementById('mode-chart')
  if (!chartDom) return
  
  if (modeChart) modeChart.dispose()
  modeChart = echarts.init(chartDom)
  
  const data = modeData.value.length > 0 ? modeData.value : [
    { name: '骑行', value: 45, color: '#10b981' },
    { name: '步行', value: 25, color: '#3b82f6' },
    { name: '公交', value: 15, color: '#f59e0b' },
    { name: '地铁', value: 10, color: '#8b5cf6' },
    { name: '其他', value: 5, color: '#6b7280' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}% ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#6b7280', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: data.map(item => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: item.color || '#6b7280' }
      }))
    }]
  }
  modeChart.setOption(option)
}

const initActivityChart = () => {
  const chartDom = document.getElementById('activity-chart')
  if (!chartDom) return
  
  if (activityChart) activityChart.dispose()
  activityChart = echarts.init(chartDom)
  
  const data = activityData.value.length > 0 ? activityData.value : [
    { name: '绿色出行月', participants: 86, target: 100 },
    { name: '无车日挑战', participants: 45, target: 60 },
    { name: '低碳知识赛', participants: 32, target: 50 }
  ]
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: { top: 10, bottom: 20, left: 80, right: 20 },
    xAxis: {
      type: 'value',
      max: 100,
      axisLine: { show: false },
      axisLabel: { show: false },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6b7280', fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: data.map(d => ({
        value: Math.round(d.participants / d.target * 100),
        participants: d.participants,
        target: d.target
      })),
      barWidth: 12,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#ec5b13' },
          { offset: 1, color: '#f59e0b' }
        ])
      },
      label: {
        show: true,
        position: 'right',
        formatter: params => `${params.data.participants}/${params.data.target}`,
        color: '#6b7280',
        fontSize: 11
      }
    }]
  }
  activityChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  modeChart?.resize()
  activityChart?.resize()
  miniCharts.forEach(chart => chart?.resize())
}

onMounted(async () => {
  await loadStats()
  await loadRecentTravels()
  await loadTopUsers()
  await loadModeData()
  await loadActivityData()
  await loadWeeklyData(parseInt(chartPeriod.value))
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
  
  refreshTimer = setInterval(refreshData, 60000)
})

watch(chartPeriod, async (newVal) => {
  await loadWeeklyData(parseInt(newVal))
  initTrendChart()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  modeChart?.dispose()
  activityChart?.dispose()
  miniCharts.forEach(chart => chart?.dispose())
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
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
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.value-number {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.value-unit {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 8px;
}

.stat-trend.up { color: #10b981; }
.stat-trend.down { color: #f97316; }
.stat-trend.neutral { color: #9ca3af; }
.stat-trend.success { color: #10b981; }
.stat-trend.warning { color: #f59e0b; }

.stat-chart {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 80px;
  height: 50px;
  opacity: 0.8;
}

.mini-chart {
  width: 100%;
  height: 100%;
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1200px) {
  .charts-row { grid-template-columns: 1fr; }
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title .total-value {
  font-size: 13px;
  color: #6b7280;
}

.header-title .total-value strong {
  color: #ec5b13;
  font-size: 15px;
}

.header-title .sub-title {
  font-size: 12px;
  color: #9ca3af;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 12px;
}

.chart-container {
  padding: 20px;
  height: 280px;
}

.trend-chart .chart-container {
  height: 320px;
}

.rank-chart {
  flex: 0.8;
}

.activity-chart {
  flex: 1;
}

.alert-chart {
  flex: 0.8;
}

.charts-row:last-child {
  grid-template-columns: 0.8fr 1fr 0.8fr;
}

@media (max-width: 1400px) {
  .charts-row:last-child {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 900px) {
  .charts-row:last-child {
    grid-template-columns: 1fr;
  }
}

.rank-list {
  padding: 12px 24px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.2s;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-item:hover {
  background: #fafafa;
  margin: 0 -24px;
  padding: 12px 24px;
}

.rank-number {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #9ca3af;
  background: #f3f4f6;
}

.rank-number.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4a 100%);
  color: #92400e;
}

.rank-number.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #595959;
}

.rank-number.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #daa06d 100%);
  color: #5c3d2e;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.rank-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.rank-value {
  text-align: right;
}

.rank-value .carbon-value {
  font-size: 18px;
  font-weight: 700;
  color: #10b981;
}

.rank-value .carbon-unit {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 2px;
}

.todo-list {
  padding: 8px 16px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.todo-item:hover {
  background: #f8fafc;
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.todo-info {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.todo-desc {
  font-size: 12px;
  color: #9ca3af;
}

.todo-count {
  min-width: 24px;
  height: 24px;
  background: #fff7ed;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.count-number {
  font-size: 12px;
  font-weight: 600;
  color: #ea580c;
}

.todo-arrow {
  color: #9ca3af;
}

.pending-badge :deep(.el-badge__content) {
  background: #ec5b13;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.user-time {
  font-size: 11px;
  color: #9ca3af;
}

.route-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.route-path {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.route-start, .route-end {
  color: #1f2937;
  font-weight: 500;
}

.route-arrow {
  color: #9ca3af;
  font-size: 12px;
}

.route-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.route-mode {
  display: flex;
  align-items: center;
  gap: 4px;
}

.carbon-cell {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.carbon-value {
  font-size: 16px;
  font-weight: 700;
  color: #10b981;
}

.carbon-unit {
  font-size: 11px;
  color: #9ca3af;
}

.points-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  color: #ec5b13;
}

.points-icon {
  font-size: 16px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.verified {
  background: #dcfce7;
  color: #166534;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.verified .status-dot { background: #10b981; }
.status-badge.pending .status-dot { background: #f59e0b; }

:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #6b7280;
  --el-table-border-color: #f3f4f6;
}

:deep(.el-table th.el-table__cell) {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 仪表盘 - 橙色文字按钮 */
:deep(.btn-orange-text) {
  color: #ec5b13 !important;
  font-weight: 500;
}
:deep(.btn-orange-text:hover) {
  color: #d14d0b !important;
  background: #fff7ed !important;
}
</style>
