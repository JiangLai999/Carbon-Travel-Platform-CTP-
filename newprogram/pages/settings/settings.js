const app = getApp();

Page({
  data: {
    userInfo: {},
    version: '1.0.0'
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({ userInfo });
    }
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/user/profile' });
  },

  clearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '确定要清除本地缓存吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorageSync();
          app.globalData.userInfo = null;
          app.globalData.token = null;
          wx.showToast({ title: '清除成功', icon: 'success' });
          this.loadUserInfo();
        }
      }
    });
  },

  goAbout() {
    wx.showModal({
      title: '关于',
      content: '低碳出行激励平台 v1.0.0\n\n致力于推动绿色出行，让每一次出行都有意义。',
      showCancel: false
    });
  },

  goFeedback() {
    wx.showModal({
      title: '意见反馈',
      editable: true,
      placeholderText: '请输入您的意见或建议',
      success: (res) => {
        if (res.confirm && res.content) {
          wx.showToast({ title: '感谢反馈', icon: 'success' });
        }
      }
    });
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
  },

  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
