const app = getApp();

Page({
  data: {
    phone: '',
    code: '',
    newPassword: '',
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

  handleReset() {
    const { phone, code, newPassword, confirmPassword } = this.data;
    if (!phone || !code || !newPassword || !confirmPassword) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }
    if (newPassword.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    app.request({
      url: '/auth/reset-password',
      method: 'POST',
      data: { phone, code, newPassword }
    }).then(() => {
      this.setData({ loading: false });
      wx.showToast({ title: '重置成功', icon: 'success' });
      setTimeout(() => wx.navigateBack({ delta: 1 }), 1500);
    }).catch((err) => {
      this.setData({ loading: false });
      wx.showToast({ title: err.message || '重置失败', icon: 'none' });
    });
  },

  goLogin() { wx.navigateBack({ delta: 1 }); }
});
