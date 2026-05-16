const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    trip: null,
    loading: true,
    modeIcons: { '步行': '🚶', '骑行': '🚴', '公交': '🚌', '地铁': '🚇', '电动车': '🛵' },
    // 地图相关
    mapLatitude: 39.9042,
    mapLongitude: 116.4074,
    mapScale: 14,
    mapMarkers: [],
    mapPolyline: [],
    hasTrackPoints: false
  },

  onLoad(options) {
    const id = options.id;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.loadDetail(id);
  },

  loadDetail(id) {
    this.setData({ loading: true });

    app.request({
      url: config.TRAVEL_RECORD_DETAIL(id),
      method: 'GET'
    }).then(data => {
      const trip = {
        ...data,
        modeIcon: this.data.modeIcons[data.travelModeName] || '🚗',
        statusText: this.getStatusText(data.status),
        statusClass: this.getStatusClass(data.status),
        timeStr: this.formatTime(data.createdAt),
        dateStr: this.formatDate(data.createdAt),
        distance: data.distance ? parseFloat(data.distance).toFixed(1) : '0.0',
        carbonReduction: data.carbonReduction ? parseFloat(data.carbonReduction).toFixed(2) : '0.00',
        pointsEarned: data.pointsEarned || 0,
        duration: data.duration || 0,
        durationStr: this.formatDuration(data.duration || 0),
        trackPoints: data.trackPoints || []
      };

      this.setData({ 
        trip, 
        loading: false,
        hasTrackPoints: trip.trackPoints && trip.trackPoints.length > 0
      });

      // 如果有轨迹点，初始化地图
      if (trip.trackPoints && trip.trackPoints.length > 0) {
        this.initMap(trip.trackPoints);
      }
    }).catch(err => {
      console.error('Failed to load trip detail:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  // 初始化地图显示轨迹
  initMap(trackPoints) {
    if (!trackPoints || trackPoints.length < 2) return;

    const startPoint = trackPoints[0];
    const endPoint = trackPoints[trackPoints.length - 1];

    // 计算地图中心点和缩放级别
    const latitudes = trackPoints.map(p => p.latitude);
    const longitudes = trackPoints.map(p => p.longitude);
    const minLat = Math.min(...latitudes);
    const maxLat = Math.max(...latitudes);
    const minLng = Math.min(...longitudes);
    const maxLng = Math.max(...longitudes);

    const centerLat = (minLat + maxLat) / 2;
    const centerLng = (minLng + maxLng) / 2;

    // 计算合适的缩放级别
    const latDiff = maxLat - minLat;
    const lngDiff = maxLng - minLng;
    const maxDiff = Math.max(latDiff, lngDiff);
    let scale = 14;
    if (maxDiff > 0.5) scale = 10;
    else if (maxDiff > 0.1) scale = 12;
    else if (maxDiff > 0.05) scale = 13;
    else if (maxDiff > 0.01) scale = 15;
    else scale = 16;

    // 构建标记点 - 使用简单标记避免渲染层错误
    const markers = [
      {
        id: 0,
        latitude: startPoint.latitude,
        longitude: startPoint.longitude,
        width: 1,
        height: 1
      },
      {
        id: 1,
        latitude: endPoint.latitude,
        longitude: endPoint.longitude,
        width: 1,
        height: 1
      }
    ];

    // 构建轨迹线 - 确保每个点都有有效的经纬度
    const validPoints = trackPoints
      .filter(p => p && typeof p.latitude === 'number' && typeof p.longitude === 'number' && 
                  !isNaN(p.latitude) && !isNaN(p.longitude))
      .map(p => ({
        latitude: p.latitude,
        longitude: p.longitude
      }));

    // 如果有效点少于2个，不显示轨迹线
    const polyline = validPoints.length >= 2 ? [{
      points: validPoints,
      color: '#10b981',
      width: 6,
      arrowLine: false
    }] : [];

    this.setData({
      mapLatitude: centerLat,
      mapLongitude: centerLng,
      mapScale: scale,
      mapMarkers: markers,
      mapPolyline: polyline
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
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return `${month}月${day}日 ${weekDays[date.getDay()]}`;
  },

  formatDuration(seconds) {
    if (!seconds || seconds < 0) return '0s';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    if (h > 0) return `${h}小时${m}分`;
    if (m > 0) return `${m}分${s}秒`;
    return `${s}秒`;
  },

  goBack() {
    wx.navigateBack();
  }
});
