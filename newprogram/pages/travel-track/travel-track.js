const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    // 记录模式: 'gps' | 'select'
    recordMode: 'select',
    selectedMode: 2,
    
    // 地图相关
    latitude: 39.9042,
    longitude: 116.4074,
    markers: [],
    polyline: [],
    
    // GPS轨迹模式
    tracking: false,
    paused: false,
    distance: 0,
    duration: 0,
    carbonReduction: 0,
    trackPoints: [],
    startTime: 0,
    pausedTime: 0,
    totalPausedDuration: 0,
    
    // 起终点选择模式
    startPoint: {},
    endPoint: {},
    startLocation: '',
    endLocation: '',
    selectDistance: 0,
    selectPoints: 0,
    selectCarbon: 0,
    selectingEndPoint: false,
    
    // 出行方式配置 (从后端动态加载)
    travelModes: [],
    modeIcons: { '步行': '🚶', '骑行': '🚴', '公交': '🚌', '地铁': '🚇', '电动车': '🛵' }
  },

  onLoad() {
    this.loadTravelModes();
    this.getLocation();
  },

  onReady() {
    if (this.data.selectDistance > 0 && this.data.travelModes.length > 0) {
      this.calculateSelectStats(this.data.selectDistance);
    }
  },

  onUnload() {
    // 页面卸载时停止定位
    this.stopLocationTracking();
  },

  // 从后端加载出行方式配置
  loadTravelModes() {
    app.request({
      url: config.USER_TRAVEL_MODES,
      method: 'GET'
    }).then(data => {
      console.log('Travel modes loaded:', data);
      const modes = (data || []).map(m => ({
        id: m.id,
        name: m.name,
        icon: this.data.modeIcons[m.name] || '🚗',
        carbonFactor: m.carbonReduction || 0,
        pointsPerKm: m.pointsPerKm || 0
      }));
      console.log('Parsed modes:', modes);
      
      if (modes.length === 0) {
        wx.showToast({ title: '暂无出行方式', icon: 'none' });
        return;
      }
      
      const validMode = modes.find(m => m.id === this.data.selectedMode);
      const selectedMode = validMode ? this.data.selectedMode : modes[0].id;
      console.log('Selected mode:', selectedMode);
      
      this.setData({ 
        travelModes: modes,
        selectedMode: selectedMode
      });
      
      if (this.data.selectDistance > 0) {
        console.log('Calculating stats for distance:', this.data.selectDistance);
        this.calculateSelectStats(this.data.selectDistance);
      }
    }).catch(err => {
      console.error('Failed to load travel modes:', err);
      wx.showToast({ title: '加载出行方式失败', icon: 'none' });
    });
  },

  // 切换记录模式
  switchRecordMode(e) {
    const mode = e.currentTarget.dataset.mode;
    this.setData({ recordMode: mode });
    this.resetSelection();
  },

  // 获取当前位置
  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
      },
      fail: (err) => {
        console.log('getLocation fail:', err);
        const sysInfo = wx.getSystemInfoSync();
        const isSimulator = !sysInfo.platform || sysInfo.platform === 'devtools';
        
        if (isSimulator) {
          // 模拟器：使用默认位置（北京天安门），不阻塞用户
          console.log('模拟器环境，使用默认位置');
          wx.showToast({ 
            title: '模拟器：使用默认位置', 
            icon: 'none',
            duration: 2000
          });
        } else {
          wx.showToast({ title: '获取位置失败，请检查定位权限', icon: 'none' });
        }
      }
    });
  },

  // 选择出行方式
  selectMode(e) {
    const mode = parseInt(e.currentTarget.dataset.mode);
    this.setData({ selectedMode: mode });
    
    // 重新计算选择模式的统计数据
    if (this.data.recordMode === 'select' && this.data.selectDistance > 0) {
      this.calculateSelectStats(this.data.selectDistance);
    }
    
    // 重新计算GPS模式的碳减排
    if (this.data.recordMode === 'gps' && this.data.distance > 0) {
      const modeData = this.data.travelModes.find(m => m.id === mode);
      const carbonFactor = modeData?.carbonFactor || 0.21;
      const carbonReduction = parseFloat((this.data.distance * carbonFactor).toFixed(2));
      this.setData({ carbonReduction });
    }
  },

  // ========== 起终点选择模式 ==========

  // 地图点击选点 (左键添加)
  onMapTap(e) {
    const { latitude, longitude } = e.detail;
    
    // GPS轨迹模式 + 模拟器：点击地图模拟移动
    if (this.data.recordMode === 'gps' && this.data.tracking && this.data._simulatorMode) {
      const point = {
        latitude,
        longitude,
        altitude: 0,
        speed: 0,
        accuracy: 10,
        timestamp: Date.now()
      };
      this.processNewTrackPoint(point);
      wx.showToast({ title: '模拟移动', icon: 'none', duration: 500 });
      return;
    }
    
    // 起终点选择模式
    if (!this.data.startPoint.latitude) {
      this.setStartPoint(latitude, longitude);
    } else if (!this.data.endPoint.latitude) {
      this.setEndPoint(latitude, longitude);
    }
  },

  // 点击标记删除 (模拟右键删除)
  onMarkerTap(e) {
    const markerId = e.markerId;
    
    wx.showActionSheet({
      itemList: ['删除此标记'],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.deleteMarker(markerId);
        }
      }
    });
  },

  // 删除标记
  deleteMarker(markerId) {
    if (markerId === 1) {
      // 删除起点
      const hasEndPoint = this.data.endPoint.latitude;
      if (hasEndPoint) {
        // 如果有终点，终点变起点
        this.setData({
          startPoint: { ...this.data.endPoint },
          endPoint: {},
          endLocation: '',
          selectDistance: 0,
          selectPoints: 0,
          selectCarbon: 0,
          markers: [{
            id: 1,
            latitude: this.data.endPoint.latitude,
            longitude: this.data.endPoint.longitude,
            width: 30,
            height: 30,
            callout: {
              content: '起点',
              color: '#ffffff',
              bgColor: '#10b981',
              fontSize: 14,
              borderRadius: 20,
              padding: 8,
              display: 'ALWAYS'
            }
          }],
          polyline: []
        });
      } else {
        // 只删除起点
        this.setData({
          startPoint: {},
          startLocation: '',
          markers: [],
          polyline: []
        });
      }
      wx.showToast({ title: '起点已删除', icon: 'none' });
    } else if (markerId === 2) {
      // 删除终点
      this.setData({
        endPoint: {},
        endLocation: '',
        selectDistance: 0,
        selectPoints: 0,
        selectCarbon: 0,
        markers: this.data.markers.filter(m => m.id !== 2),
        polyline: []
      });
      wx.showToast({ title: '终点已删除', icon: 'none' });
    }
  },

  // 设置起点
  setStartPoint(latitude, longitude) {
    const startPoint = { latitude, longitude };
    const markers = [{
      id: 1,
      latitude,
      longitude,
      width: 30,
      height: 30,
      callout: {
        content: '起点',
        color: '#ffffff',
        bgColor: '#10b981',
        fontSize: 14,
        borderRadius: 20,
        padding: 8,
        display: 'ALWAYS'
      }
    }];
    
    this.setData({ startPoint, markers, selectingEndPoint: true });
    this.reverseGeocoder(latitude, longitude, 'start');
    wx.showToast({ title: '起点已选择', icon: 'success' });
  },

  // 设置终点
  setEndPoint(latitude, longitude) {
    const endPoint = { latitude, longitude };
    const { startPoint } = this.data;
    
    // 添加终点标记
    const markers = [...this.data.markers, {
      id: 2,
      latitude,
      longitude,
      width: 30,
      height: 30,
      callout: {
        content: '终点',
        color: '#ffffff',
        bgColor: '#ef4444',
        fontSize: 14,
        borderRadius: 20,
        padding: 8,
        display: 'ALWAYS'
      }
    }];
    
    // 绘制直线 - 使用正确的polyline格式
    const polyline = [{
      points: [
        { latitude: startPoint.latitude, longitude: startPoint.longitude },
        { latitude: latitude, longitude: longitude }
      ],
      color: '#10b981FF',
      width: 4,
      dottedLine: true
    }];
    
    // 计算距离
    const distance = this.calculateDistance(
      startPoint.latitude, startPoint.longitude,
      latitude, longitude
    );
    
    this.setData({ 
      endPoint, 
      markers, 
      polyline, 
      selectDistance: distance,
      selectingEndPoint: false 
    });
    
    this.calculateSelectStats(distance);
    
    this.reverseGeocoder(latitude, longitude, 'end');
    wx.showToast({ title: '终点已选择', icon: 'success' });
  },

  // 使用当前位置
  useCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        if (!this.data.startPoint.latitude) {
          this.setStartPoint(res.latitude, res.longitude);
        } else if (!this.data.endPoint.latitude) {
          this.setEndPoint(res.latitude, res.longitude);
        }
      },
      fail: (err) => {
        console.log('useCurrentLocation fail:', err);
        const sysInfo = wx.getSystemInfoSync();
        const isSimulator = !sysInfo.platform || sysInfo.platform === 'devtools';
        
        if (isSimulator) {
          // 模拟器：使用默认位置
          console.log('模拟器环境，使用默认位置作为当前位置');
          const defaultLat = 39.9042;
          const defaultLng = 116.4074;
          if (!this.data.startPoint.latitude) {
            this.setStartPoint(defaultLat, defaultLng);
          } else if (!this.data.endPoint.latitude) {
            this.setEndPoint(defaultLat, defaultLng);
          }
          wx.showToast({ 
            title: '模拟器：使用默认位置', 
            icon: 'none',
            duration: 1500
          });
        } else {
          wx.showToast({ title: '获取位置失败，请检查定位权限', icon: 'none' });
        }
      }
    });
  },

  // 选择起点位置
  selectStartLocation() {
    wx.chooseLocation({
      success: (res) => {
        this.setStartPoint(res.latitude, res.longitude);
        this.setData({ startLocation: res.name || res.address });
      },
      fail: () => {
        wx.showToast({ title: '选择位置取消', icon: 'none' });
      }
    });
  },

  // 选择终点位置
  selectEndLocation() {
    if (!this.data.startPoint.latitude) {
      wx.showToast({ title: '请先选择起点', icon: 'none' });
      return;
    }
    wx.chooseLocation({
      success: (res) => {
        this.setEndPoint(res.latitude, res.longitude);
        this.setData({ endLocation: res.name || res.address });
      },
      fail: () => {
        wx.showToast({ title: '选择位置取消', icon: 'none' });
      }
    });
  },

  // 重置选择
  resetSelection() {
    this.setData({
      startPoint: {},
      endPoint: {},
      startLocation: '',
      endLocation: '',
      selectDistance: 0,
      selectPoints: 0,
      selectCarbon: 0,
      markers: [],
      polyline: [],
      selectingEndPoint: false
    });
  },

  // 计算选择模式的统计数据
  calculateSelectStats(distance) {
    console.log('calculateSelectStats called with distance:', distance);
    console.log('travelModes:', this.data.travelModes);
    console.log('selectedMode:', this.data.selectedMode);
    
    if (!distance || distance <= 0) {
      this.setData({
        selectPoints: 0,
        selectCarbon: 0,
        selectDistance: 0
      });
      return;
    }
    
    // 如果 travelModes 还没加载完成，等待后再计算
    if (this.data.travelModes.length === 0) {
      console.log('travelModes not loaded yet, waiting...');
      setTimeout(() => {
        this.calculateSelectStats(distance);
      }, 200);
      return;
    }
    
    let mode = this.data.travelModes.find(m => m.id === this.data.selectedMode);
    if (!mode && this.data.travelModes.length > 0) {
      mode = this.data.travelModes[0];
    }
    
    if (!mode) {
      console.log('No mode found, setting to 0');
      this.setData({
        selectPoints: 0,
        selectCarbon: 0
      });
      return;
    }
    
    console.log('Using mode:', mode);
    const pointsPerKm = mode.pointsPerKm || 10;
    const carbonFactor = mode.carbonFactor || 0.21;
    
    const points = Math.floor(distance * pointsPerKm);
    const carbon = distance * carbonFactor;
    
    console.log('Calculated points:', points, 'carbon:', carbon);
    
    this.setData({
      selectDistance: parseFloat(distance.toFixed(2)),
      selectPoints: isNaN(points) ? 0 : Math.max(0, points),
      selectCarbon: isNaN(carbon) ? 0 : parseFloat(carbon.toFixed(2))
    });
  },

  // 逆地理编码获取地址
  reverseGeocoder(latitude, longitude, type) {
    const coordStr = `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`;
    if (type === 'start') {
      this.setData({ startLocation: coordStr });
    } else {
      this.setData({ endLocation: coordStr });
    }
  },

  // 提交选择模式的记录
  submitSelectRecord() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { startPoint, endPoint, selectDistance, selectedMode, startLocation, endLocation } = this.data;
    
    if (!startPoint.latitude || !endPoint.latitude) {
      wx.showToast({ title: '请选择起终点', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    app.request({
      url: config.TRAVEL_RECORD,
      method: 'POST',
      data: {
        travelModeId: selectedMode,
        startLocation: startLocation || `${startPoint.latitude.toFixed(4)}, ${startPoint.longitude.toFixed(4)}`,
        endLocation: endLocation || `${endPoint.latitude.toFixed(4)}, ${endPoint.longitude.toFixed(4)}`,
        distance: selectDistance
      }
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack({ delta: 1 });
      }, 1500);
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    });
  },

  // 处理新的轨迹点
  processNewTrackPoint(point) {
    console.log('processNewTrackPoint called:', point);
    const trackPoints = [...this.data.trackPoints, point];
    console.log('trackPoints length:', trackPoints.length);

    let distance = this.data.distance;
    if (trackPoints.length > 1) {
      const prev = trackPoints[trackPoints.length - 2];
      const segmentDistance = this.calculateDistance(
        prev.latitude, prev.longitude,
        point.latitude, point.longitude
      );
      console.log('segmentDistance:', segmentDistance);
      // 过滤GPS漂移：只累计合理的距离变化
      if (segmentDistance > 0.001 && segmentDistance < 1) {
        distance += segmentDistance;
      }
    }

    // 更新轨迹线
    const polylinePoints = trackPoints
      .filter(p => p && typeof p.latitude === 'number' && typeof p.longitude === 'number' &&
                  !isNaN(p.latitude) && !isNaN(p.longitude))
      .map(p => ({
        latitude: p.latitude,
        longitude: p.longitude
      }));
    console.log('polylinePoints:', polylinePoints);

    const duration = Math.floor((Date.now() - this.data.startTime - this.data.totalPausedDuration) / 1000);
    const mode = this.data.travelModes.find(m => m.id === this.data.selectedMode);
    const carbonReduction = parseFloat((distance * (mode?.carbonFactor || 0.21)).toFixed(2));

    this.setData({
      trackPoints,
      distance,
      duration,
      carbonReduction,
      latitude: point.latitude,
      longitude: point.longitude,
      polyline: polylinePoints.length >= 2 ? [{
        points: polylinePoints,
        color: '#10b981',
        width: 6
      }] : []
    }, () => {
      console.log('setData callback, trackPoints:', this.data.trackPoints.length);
    });
  },

  // ========== GPS轨迹模式 ==========

  // 开始追踪
  startTracking() {
    this.setData({
      tracking: true,
      paused: false,
      startTime: Date.now(),
      trackPoints: [],
      distance: 0,
      duration: 0,
      carbonReduction: 0,
      markers: [{
        id: 0,
        latitude: this.data.latitude,
        longitude: this.data.longitude,
        width: 20,
        height: 20,
        callout: {
          content: '起点',
          color: '#ffffff',
          bgColor: '#10b981',
          fontSize: 12,
          borderRadius: 15,
          padding: 6,
          display: 'ALWAYS'
        }
      }],
      polyline: [{
        points: [{
          latitude: this.data.latitude,
          longitude: this.data.longitude
        }],
        color: '#10b981',
        width: 6
      }]
    });

    this.startLocationTracking();
  },

  // 开始位置追踪
  startLocationTracking() {
    let isSimulator = false;
    try {
      const sysInfo = wx.getSystemInfoSync();
      isSimulator = !sysInfo.platform || sysInfo.platform === 'devtools';
    } catch (e) {
      console.warn('getSystemInfoSync failed, assuming simulator');
      isSimulator = true;
    }
    
    if (isSimulator) {
      // 模拟器：使用定时器模拟位置更新，让用户点击地图来模拟移动
      console.log('模拟器环境：启用点击地图模拟移动模式');
      this.setData({
        _simulatorMode: true
      });
      wx.showToast({ 
        title: '模拟器模式：点击地图模拟移动', 
        icon: 'none',
        duration: 3000
      });
      return;
    }

    // 真机：使用真实的定位API
    wx.startLocationUpdate({
      success: () => {
        this.locationChangeCallback = (res) => {
          if (this.data.paused) return;
          
          const point = {
            latitude: res.latitude,
            longitude: res.longitude,
            altitude: res.altitude || 0,
            speed: res.speed || 0,
            accuracy: res.accuracy || 0,
            timestamp: Date.now()
          };

          this.processNewTrackPoint(point);
        };
        
        wx.onLocationChange(this.locationChangeCallback);
      },
      fail: (err) => {
        console.error('startLocationUpdate failed:', err);
        wx.showToast({ title: '定位启动失败', icon: 'none' });
      }
    });
  },

  // 停止位置追踪
  stopLocationTracking() {
    if (this.locationChangeCallback) {
      wx.offLocationChange(this.locationChangeCallback);
      this.locationChangeCallback = null;
    }
    wx.stopLocationUpdate({
      fail: () => {}
    });
    // 清除模拟器定时器
    if (this.simulatorTimer) {
      clearInterval(this.simulatorTimer);
      this.simulatorTimer = null;
    }
  },

  // 暂停追踪
  pauseTracking() {
    this.stopLocationTracking();
    this.setData({
      paused: true,
      pausedTime: Date.now()
    });
  },

  // 继续追踪
  resumeTracking() {
    const pausedDuration = Date.now() - this.data.pausedTime;
    this.setData({
      paused: false,
      totalPausedDuration: this.data.totalPausedDuration + pausedDuration
    });
    this.startLocationTracking();
  },

  // 停止追踪
  stopTracking() {
    this.stopLocationTracking();

    const trackPoints = this.data.trackPoints;
    console.log('stopTracking called, trackPoints length:', trackPoints.length);
    console.log('trackPoints:', trackPoints);

    if (trackPoints.length < 2) {
      wx.showToast({ title: '轨迹数据不足（至少需要2个点）', icon: 'none' });
      this.setData({ tracking: false });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    const startPoint = trackPoints[0];
    const endPoint = trackPoints[trackPoints.length - 1];
    const duration = Math.floor((Date.now() - this.data.startTime - this.data.totalPausedDuration) / 1000);

    // 构建完整轨迹数据
    const trackData = {
      travelModeId: this.data.selectedMode,
      startLocation: this.data.startLocation || `${startPoint.latitude.toFixed(4)}, ${startPoint.longitude.toFixed(4)}`,
      endLocation: `${endPoint.latitude.toFixed(4)}, ${endPoint.longitude.toFixed(4)}`,
      distance: parseFloat(this.data.distance.toFixed(2)),
      duration: duration,
      carbonReduction: this.data.carbonReduction,
      // 完整轨迹点数组
      trackPoints: trackPoints.map((p, index) => ({
        latitude: p.latitude,
        longitude: p.longitude,
        timestamp: p.timestamp,
        sequence: index + 1,
        speed: p.speed || 0,
        altitude: p.altitude || 0,
        accuracy: p.accuracy || 0
      }))
    };

    // 先提交行程记录
    app.request({
      url: config.TRAVEL_RECORD,
      method: 'POST',
      data: {
        travelModeId: trackData.travelModeId,
        startLocation: trackData.startLocation,
        endLocation: trackData.endLocation,
        distance: trackData.distance,
        duration: trackData.duration,
        carbonReduction: trackData.carbonReduction
      }
    }).then(res => {
      const recordId = res?.id || res?.data?.id;
      
      // 如果有记录ID且后端支持轨迹接口，提交完整轨迹
      if (recordId && config.TRAVEL_TRACK) {
        return app.request({
          url: config.TRAVEL_TRACK,
          method: 'POST',
          data: {
            recordId: recordId,
            points: trackData.trackPoints,
            totalPoints: trackData.trackPoints.length
          }
        }).then(() => recordId).catch(err => {
          console.log('轨迹提交失败（可能后端未实现）:', err);
          return recordId;
        });
      }
      return recordId;
    }).then(recordId => {
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack({ delta: 1 });
      }, 1500);
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    });
  },

  // ========== 工具函数 ==========

  // 计算两点距离 (Haversine公式)
  calculateDistance(lat1, lng1, lat2, lng2) {
    // 验证输入
    if (!lat1 || !lng1 || !lat2 || !lng2) return 0;
    if (lat1 === lat2 && lng1 === lng2) return 0;
    
    const R = 6371; // 地球半径(km)
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    
    // 确保返回有效值
    return isNaN(distance) || distance < 0 ? 0 : distance;
  },

  // 格式化时长
  formatDuration(seconds) {
    if (!seconds || seconds < 0) return '0s';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    if (h > 0) return `${h}h ${m}m`;
    if (m > 0) return `${m}m ${s}s`;
    return `${s}s`;
  }
});
