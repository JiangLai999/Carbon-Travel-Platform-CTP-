const config = require('./config.js');

App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: config.API_BASE
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');

    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
    // 不在 onLaunch 中跳转，由各页面自行判断是否需要登录
  },

  request(options) {
    const token = this.globalData.token || wx.getStorageSync('token');
    const baseUrl = this.globalData.baseUrl;
    const header = {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };

    // 自动拼接 baseUrl（相对路径才拼接）
    const url = options.url.startsWith('http')
      ? options.url
      : baseUrl + (options.url.startsWith('/') ? options.url : '/' + options.url);

    return new Promise((resolve, reject) => {
      wx.request({
        ...options,
        url,
        header,
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data.data);
          } else if (res.data.code === 401) {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            this.globalData.token = null;
            this.globalData.userInfo = null;
            wx.reLaunch({ url: '/pages/auth/login' });
            reject(new Error('未授权，请重新登录'));
          } else {
            reject(new Error(res.data.message || '请求失败'));
          }
        },
        fail: (err) => {
          reject(new Error('网络错误，请检查后端服务是否启动'));
        }
      });
    });
  },

  // 处理图片URL，将相对路径转换为完整URL
  getImageUrl(imageUrl) {
    if (!imageUrl) return '';
    // 已经是完整URL
    if (imageUrl.startsWith('http')) return imageUrl;
    // 拼接完整路径（保留/api前缀，因为后端context-path是/api）
    if (imageUrl.startsWith('/')) {
      return config.API_BASE + imageUrl;
    }
    return config.API_BASE + '/' + imageUrl;
  },

  // 处理用户头像URL（支持默认头像显示）
  getAvatarUrl(avatar) {
    if (!avatar) return '';
    return this.getImageUrl(avatar);
  },

  setUser(userInfo, token) {
    this.globalData.userInfo = userInfo;
    this.globalData.token = token;
    wx.setStorageSync('userInfo', userInfo);
    wx.setStorageSync('token', token);
  },

  logout() {
    this.globalData.userInfo = null;
    this.globalData.token = null;
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('token');
    wx.reLaunch({ url: '/pages/auth/login' });
  }
});
