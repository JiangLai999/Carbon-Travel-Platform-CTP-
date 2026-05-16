const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    phone: '',
    password: '',
    code: '',
    loginType: 'password', // 'password' | 'code'
    showPassword: false,
    loading: false,
    countdown: 0
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  switchTab(e) {
    this.setData({ loginType: e.currentTarget.dataset.type, code: '', password: '' });
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword });
  },

  sendCode() {
    const { phone, countdown } = this.data;
    if (countdown > 0) return;
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    app.request({
      url: config.AUTH_SEND_CODE,
      method: 'POST',
      data: { phone }
    }).then((res) => {
      wx.showToast({ title: '验证码已发送', icon: 'success' });
      // 开发阶段：直接填入验证码
      if (res.code) this.setData({ code: res.code });
      this._startCountdown();
    }).catch((err) => {
      wx.showToast({ title: err.message || '发送失败', icon: 'none' });
    });
  },

  _startCountdown() {
    this.setData({ countdown: 60 });
    const timer = setInterval(() => {
      const c = this.data.countdown - 1;
      if (c <= 0) {
        clearInterval(timer);
        this.setData({ countdown: 0 });
      } else {
        this.setData({ countdown: c });
      }
    }, 1000);
  },

  handleLogin() {
    const { phone, password, code, loginType } = this.data;
    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (loginType === 'password' && !password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
    if (loginType === 'code' && !code) {
      wx.showToast({ title: '请输入验证码', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    const data = loginType === 'code'
      ? { phone, code, loginType: 'code' }
      : { phone, password, loginType: 'password' };
    app.request({
      url: '/auth/login',
      method: 'POST',
      data
    }).then((res) => {
      this.setData({ loading: false });
      const userInfo = { 
        userId: res.userId, 
        phone: res.phone, 
        nickname: res.nickname, 
        avatar: app.getAvatarUrl(res.avatar)
      };
      app.setUser(userInfo, res.token);
      wx.reLaunch({ url: '/pages/index/index' });
    }).catch((err) => {
      this.setData({ loading: false });
      wx.showToast({ title: err.message || '登录失败', icon: 'none' });
    });
  },

  goRegister() { wx.navigateTo({ url: '/pages/auth/register' }); },
  goReset() { wx.navigateTo({ url: '/pages/auth/reset' }); },
  goBack() { wx.navigateBack({ delta: 1 }); }
});
