const app = getApp();

Page({
  data: {
    phone: '',
    code: '',
    password: '',
    confirmPassword: '',
    loading: false,
    countdown: 0
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  sendCode() {
    const { phone, countdown } = this.data;
    if (countdown > 0) return;
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    app.request({
      url: '/auth/send-code',
      method: 'POST',
      data: { phone }
    }).then((res) => {
      wx.showToast({ title: '验证码已发送', icon: 'success' });
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
      if (c <= 0) { clearInterval(timer); this.setData({ countdown: 0 }); }
      else this.setData({ countdown: c });
    }, 1000);
  },

  handleRegister() {
    const { phone, code, password, confirmPassword } = this.data;
    if (!phone || !code || !password || !confirmPassword) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    app.request({
      url: '/auth/register',
      method: 'POST',
      data: { phone, code, password, confirmPassword }
    }).then((data) => {
      this.setData({ loading: false });
      wx.showToast({ title: '注册成功', icon: 'success' });
      setTimeout(() => {
        app.setUser({ 
          userId: data.userId, 
          phone: data.phone, 
          nickname: data.nickname, 
          avatar: app.getAvatarUrl(data.avatar)
        }, data.token);
        wx.reLaunch({ url: '/pages/index/index' });
      }, 1500);
    }).catch((err) => {
      this.setData({ loading: false });
      wx.showToast({ title: err.message || '注册失败', icon: 'none' });
    });
  },

  goLogin() { wx.navigateTo({ url: '/pages/auth/login' }); }
});
