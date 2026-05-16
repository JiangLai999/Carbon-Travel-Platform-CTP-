const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    activities: [],
    loading: false
  },

  onLoad() {
    this.loadMyActivities();
  },

  onShow() {
    this.loadMyActivities();
  },

  loadMyActivities() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    app.request({
      url: config.USER_JOINED_ACTIVITIES + '?page=1&size=50',
      method: 'GET'
    }).then(data => {
      const activities = (data?.records || data || []).map(a => ({
        ...a,
        statusText: this.getStatusText(a.status),
        statusClass: this.getStatusClass(a.status),
        timeStr: this.formatTime(a.createdAt)
      }));
      this.setData({ activities, loading: false });
    }).catch(err => {
      console.error('Failed to load activities:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
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
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}-${day}`;
  },

  goActivityList() {
    wx.navigateTo({ url: '/pages/activity/activity' });
  },

  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
