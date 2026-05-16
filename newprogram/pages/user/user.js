const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    userInfo: { 
      id: '',
      nickname: '低碳用户', 
      avatar: '',
      phone: '',
      realName: '',
      idCard: '',
      idCardMasked: '',
      email: '',
      role: 'user',
      status: 1
    },
    userLevel: '新芽',
    stats: {
      travelCount: 0,
      totalCarbon: 0,
      availablePoints: 0,
      activityCount: 0,
      orderCount: 0,
      postCount: 0,
      rank: 0
    },
    unreadCount: 0
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    if (typeof this.getTabBar === 'function') {
      this.getTabBar().setData({ selected: 4 });
    }
    this.loadUserInfo();
    this.loadStats();
    this.loadUnreadCount();
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
    if (userInfo) {
      userInfo.idCardMasked = this.maskIdCard(userInfo.idCard);
      this.setData({ userInfo });
    }
    
    const token = wx.getStorageSync('token');
    if (!token) return;
    
    app.request({
      url: config.USER_PROFILE,
      method: 'GET'
    }).then(data => {
      if (data) {
        const userInfo = {
          id: data.id,
          nickname: data.nickname || '低碳用户',
          avatar: app.getAvatarUrl(data.avatar),
          phone: data.phone || '',
          realName: data.realName || '',
          idCard: data.idCard || '',
          idCardMasked: this.maskIdCard(data.idCard),
          email: data.email || '',
          role: data.role || 'user',
          status: data.status || 1
        };
        this.setData({ userInfo });
        app.globalData.userInfo = userInfo;
        wx.setStorageSync('userInfo', userInfo);
      }
    }).catch(err => {
      console.error('Failed to load user info:', err);
    });
  },

  maskIdCard(idCard) {
    if (!idCard || idCard.length < 10) return '';
    return idCard.substring(0, 3) + '***********' + idCard.substring(idCard.length - 4);
  },

  calculateLevel(points) {
    if (points >= 1000) return '金叶子';
    if (points >= 500) return '银叶子';
    if (points >= 100) return '绿叶';
    return '新芽';
  },

  loadStats() {
    const token = wx.getStorageSync('token');
    if (!token) return;
    
    app.request({
      url: config.USER_STATS,
      method: 'GET'
    }).then(data => {
      if (data) {
        const availablePoints = Math.floor(data.availablePoints || 0);
        this.setData({ 
          stats: {
            travelCount: data.travelCount || 0,
            totalCarbon: (data.totalCarbon || 0).toFixed(2),
            availablePoints: availablePoints,
            activityCount: data.activityCount || 0,
            orderCount: data.orderCount || 0,
            postCount: data.postCount || 0,
            rank: data.rank || 0
          },
          userLevel: this.calculateLevel(availablePoints)
        });
      }
    }).catch(err => {
      console.error('Failed to load stats:', err);
    });
  },

  loadUnreadCount() {
    const token = wx.getStorageSync('token');
    if (!token) return;
    
    app.request({
      url: config.MESSAGES_UNREAD_COUNT,
      method: 'GET'
    }).then(data => {
      this.setData({ unreadCount: data || 0 });
    }).catch(err => {
      console.error('Failed to load unread count:', err);
    });
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/user/profile' });
  },

  goMyTrips() {
    wx.navigateTo({ url: '/pages/my-trips/my-trips' });
  },

  goPoints() {
    wx.navigateTo({ url: '/pages/points/points' });
  },

  goMyActivities() {
    wx.navigateTo({ url: '/pages/my-activities/my-activities' });
  },

  goMyPosts() {
    wx.navigateTo({ url: '/pages/my-posts/my-posts' });
  },

  goMessages() {
    wx.navigateTo({ url: '/pages/message/message' });
  },

  goSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' });
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
        }
      }
    });
  }
});
