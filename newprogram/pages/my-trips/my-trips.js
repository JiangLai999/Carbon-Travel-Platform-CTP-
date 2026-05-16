const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    trips: [],
    filteredTrips: [],
    groupedTrips: [],
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 20,
    filterType: 'all',
    stats: {
      totalTrips: 0,
      totalDistance: '0.0',
      totalCarbon: '0.0'
    },
    modeIcons: { '步行': '🚶', '骑行': '🚴', '公交': '🚌', '地铁': '🚇', '电动车': '🛵' }
  },

  onLoad() {
    this.loadTrips();
  },

  onShow() {
    this.loadTrips();
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true });
    this.loadTrips().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore();
    }
  },

  loadTrips() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return Promise.resolve();
    }

    this.setData({ loading: true });
    
    return app.request({
      url: config.TRAVEL_RECORDS + `?page=${this.data.page}&size=${this.data.pageSize}`,
      method: 'GET'
    }).then(data => {
      const records = data?.records || data || [];
      const newTrips = records.map(t => ({
        ...t,
        modeIcon: this.data.modeIcons[t.travelModeName] || '🚗',
        statusText: this.getStatusText(t.status),
        timeStr: this.formatTime(t.createdAt),
        distance: t.distance ? parseFloat(t.distance).toFixed(1) : '0.0',
        carbonReduction: t.carbonReduction ? parseFloat(t.carbonReduction).toFixed(2) : '0.00',
        hasTrackPoints: !!(t.trackPoints && t.trackPoints.length > 0)
      }));

      let allTrips;
      if (this.data.page === 1) {
        allTrips = newTrips;
      } else {
        allTrips = [...this.data.trips, ...newTrips];
      }

      this.setData({
        trips: allTrips,
        hasMore: newTrips.length >= this.data.pageSize,
        loading: false
      });

      this.applyFilter();
      this.calculateStats(allTrips);
    }).catch(err => {
      console.error('Failed to load trips:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  loadMore() {
    this.setData({ page: this.data.page + 1 });
    this.loadTrips();
  },

  // 筛选功能
  setFilter(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ filterType: type });
    this.applyFilter();
  },

  applyFilter() {
    let filtered = [...this.data.trips];
    const { filterType } = this.data;

    if (filterType === 'approved') {
      filtered = filtered.filter(t => t.status === 1);
    } else if (filterType === 'pending') {
      filtered = filtered.filter(t => t.status === 0);
    } else if (filterType === 'rejected') {
      filtered = filtered.filter(t => t.status === 2);
    }

    this.setData({ filteredTrips: filtered });
    this.groupTripsByDate(filtered);
  },

  // 按日期分组
  groupTripsByDate(trips) {
    const groups = {};
    
    trips.forEach(trip => {
      const date = this.formatDate(trip.createdAt);
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(trip);
    });

    const groupedTrips = Object.keys(groups).map(date => ({
      date,
      trips: groups[date]
    }));

    this.setData({ groupedTrips });
  },

  // 计算统计数据
  calculateStats(trips) {
    let totalDistance = 0;
    let totalCarbon = 0;

    trips.forEach(trip => {
      totalDistance += parseFloat(trip.distance) || 0;
      totalCarbon += parseFloat(trip.carbonReduction) || 0;
    });

    this.setData({
      stats: {
        totalTrips: trips.length,
        totalDistance: totalDistance.toFixed(1),
        totalCarbon: totalCarbon.toFixed(1)
      }
    });
  },

  getStatusText(status) {
    const map = { 0: '待审核', 1: '已通过', 2: '已驳回' };
    return map[status] || '未知';
  },

  getStatusClass(status) {
    const map = { 0: 'pending', 1: 'approved', 2: 'rejected' };
    return map[status] || '';
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  },

  formatDate(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today.getTime() - 86400000);
    const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const weekDay = weekDays[date.getDay()];

    if (targetDate.getTime() === today.getTime()) {
      return `今天 ${weekDay}`;
    } else if (targetDate.getTime() === yesterday.getTime()) {
      return `昨天 ${weekDay}`;
    } else if (date.getFullYear() === now.getFullYear()) {
      return `${month}月${day}日 ${weekDay}`;
    } else {
      return `${date.getFullYear()}-${month}-${day}`;
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/travel-detail/travel-detail?id=${id}` });
  },

  goRecord() {
    wx.navigateTo({ url: '/pages/travel-track/travel-track' });
  },

  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
