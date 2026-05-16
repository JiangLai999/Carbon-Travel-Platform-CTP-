const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    selectedMode: null,
    form: {
      startLocation: '',
      endLocation: '',
      distance: ''
    },
    records: [],
    travelModes: [],
    estimatedCarbon: '0.00',
    estimatedPoints: 0
  },

  onLoad() {
    this.loadTravelModes();
    this.loadRecords();
  },

  onShow() {
    if (typeof this.getTabBar === 'function') {
      this.getTabBar().setData({ selected: 1 });
    }
    this.loadRecords();
  },

  loadTravelModes() {
    app.request({
      url: config.USER_TRAVEL_MODES,
      method: 'GET'
    }).then(data => {
      const modeIcons = { '步行': '🚶', '骑行': '🚴', '公交': '🚌', '地铁': '🚇', '电动车': '🛵' };
      const modes = (data || []).map(m => ({ ...m, icon: modeIcons[m.name] || '🚗' }));
      this.setData({ travelModes: modes });
    }).catch(err => {
      console.error('Failed to load travel modes:', err);
    });
  },

  selectMode(e) {
    const mode = parseInt(e.currentTarget.dataset.mode);
    this.setData({ selectedMode: mode });
    this.calculateEstimate();
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    const form = this.data.form;
    form[field] = e.detail.value;
    this.setData({ form });
    this.calculateEstimate();
  },

  // 计算预计收益
  calculateEstimate() {
    const { selectedMode, form, travelModes } = this.data;
    const distance = parseFloat(form.distance) || 0;
    
    if (!selectedMode || distance <= 0) {
      this.setData({ estimatedCarbon: '0.00', estimatedPoints: 0 });
      return;
    }

    const mode = travelModes.find(m => m.id === selectedMode);
    if (!mode) return;

    const carbonReduction = (mode.carbonReduction || 0) * distance;
    const points = Math.floor((mode.pointsPerKm || 0) * distance);

    this.setData({
      estimatedCarbon: carbonReduction.toFixed(2),
      estimatedPoints: points
    });
  },

  submitRecord() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { selectedMode, form } = this.data;
    if (!selectedMode) {
      wx.showToast({ title: '请选择出行方式', icon: 'none' });
      return;
    }
    if (!form.startLocation || !form.endLocation) {
      wx.showToast({ title: '请填写出发地和目的地', icon: 'none' });
      return;
    }
    if (!form.distance || parseFloat(form.distance) <= 0) {
      wx.showToast({ title: '请输入有效距离', icon: 'none' });
      return;
    }

    const modeObj = this.data.travelModes.find(m => m.id === selectedMode);
    const payload = {
      travelModeId: selectedMode,
      travelModeName: modeObj?.name,
      startLocation: form.startLocation,
      endLocation: form.endLocation,
      distance: parseFloat(form.distance)
    };

    wx.showLoading({ title: '提交中...' });
    app.request({
      url: config.TRAVEL_RECORD,
      method: 'POST',
      data: payload
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({
        selectedMode: null,
        form: { startLocation: '', endLocation: '', distance: '' },
        estimatedCarbon: '0.00',
        estimatedPoints: 0
      });
      this.loadRecords();
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    });
  },

  loadRecords() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    app.request({
      url: config.TRAVEL_RECORDS + '?page=1&size=5',
      method: 'GET'
    }).then(data => {
      const records = (data?.records || data || []).map(r => ({
        ...r,
        createdAt: this.formatTime(r.createdAt)
      }));
      this.setData({ records });
    }).catch(err => {
      console.error('Failed to load records:', err);
    });
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    return `${month}-${day} ${hour}:${minute}`;
  },

  goTrack() {
    wx.navigateTo({ url: '/pages/travel-track/travel-track' });
  },

  goMyTrips() {
    wx.navigateTo({ url: '/pages/my-trips/my-trips' });
  }
});
