const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    activeTab: 'all',
    tabs: [
      { id: 'all', name: '全部活动' },
      { id: 'ongoing', name: '进行中' },
      { id: 'joined', name: '已参与' }
    ],
    activities: [],
    filteredActivities: [],
    joinedActivities: [],
    joinedActivityIds: []  // 已参与的活动ID列表
  },

  onLoad() {
    this.loadActivities();
  },

  onShow() {
    this.loadActivities();
    this.loadJoinedActivities();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab });
    this.filterActivities();
  },

  loadActivities() {
    app.request({
      url: config.ACTIVITY_LIST + '?page=1&size=20',
      method: 'GET'
    }).then(data => {
      const activities = (data?.records || data || []).map(a => ({
        ...a,
        imageUrl: app.getImageUrl(a.imageUrl),
        statusText: this.getStatusText(a),
        dateRange: `${this.formatDate(a.startDate)} - ${this.formatDate(a.endDate)}`,
        isJoined: this.data.joinedActivityIds.includes(a.id)
      }));
      this.setData({ activities });
      this.filterActivities();
    }).catch(err => {
      console.error('Failed to load activities:', err);
    });
  },

  loadJoinedActivities() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    app.request({
      url: config.USER_JOINED_ACTIVITIES + '?page=1&size=20',
      method: 'GET'
    }).then(data => {
      const joinedActivities = (data?.records || data || []).map(a => ({
        ...a,
        statusText: this.getJoinedStatusText(a.status),
        statusClass: this.getStatusClass(a.status)
      }));
      const joinedActivityIds = joinedActivities.map(a => a.activityId);
      this.setData({ joinedActivities, joinedActivityIds });
      // 重新加载活动列表以更新参与状态
      this.loadActivities();
    }).catch(err => {
      console.error('Failed to load joined activities:', err);
    });
  },

  filterActivities() {
    const { activities, activeTab, joinedActivityIds } = this.data;
    let filtered = activities;

    if (activeTab === 'ongoing') {
      filtered = activities.filter(a => a.statusText === '进行中');
    } else if (activeTab === 'joined') {
      filtered = activities.filter(a => joinedActivityIds.includes(a.id));
    }

    this.setData({ filteredActivities: filtered });
  },

  getStatusText(activity) {
    const now = new Date();
    const start = new Date(activity.startDate);
    const end = new Date(activity.endDate);
    
    if (now < start) return '未开始';
    if (now > end) return '已结束';
    return '进行中';
  },

  getJoinedStatusText(status) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已驳回'
    };
    return statusMap[status] || '未知';
  },

  getStatusClass(status) {
    const classMap = {
      0: 'pending',
      1: 'approved',
      2: 'rejected'
    };
    return classMap[status] || '';
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  },

  joinActivity(e) {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认报名',
      content: '确定要报名参加此活动吗？',
      success: (res) => {
        if (res.confirm) {
          this.doJoinActivity(id);
        }
      }
    });
  },

  doJoinActivity(id) {
    wx.showLoading({ title: '报名中...' });
    app.request({
      url: config.ACTIVITY_JOIN(id),
      method: 'POST'
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '报名成功', icon: 'success' });
      this.loadJoinedActivities();
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '报名失败', icon: 'none' });
    });
  },

  submitProof(e) {
    const participationId = e.currentTarget.dataset.participationId;
    const activityId = e.currentTarget.dataset.activityId;
    wx.navigateTo({ url: `/pages/activity/proof?participationId=${participationId}&activityId=${activityId}` });
  },

  goActivityDetail(e) {
    const id = e.currentTarget.dataset.id;
    // 可以跳转到活动详情页（如果有的话）
    console.log('Activity detail:', id);
  }
});
