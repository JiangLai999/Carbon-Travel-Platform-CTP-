const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    userInfo: {},
    loading: false
  },

  onLoad() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
    if (userInfo) {
      if (userInfo.avatar) {
        userInfo.avatar = app.getImageUrl(userInfo.avatar);
      }
      this.setData({ userInfo: { ...userInfo } });
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
          nickname: data.nickname || '',
          avatar: app.getImageUrl(data.avatar),
          phone: data.phone || '',
          realName: data.realName || '',
          idCard: data.idCard || '',
          email: data.email || '',
          deliveryAddress: data.deliveryAddress || '',
          deliveryName: data.deliveryName || '',
          deliveryPhone: data.deliveryPhone || '',
          province: data.province || '',
          city: data.city || '',
          district: data.district || '',
          role: data.role || 'user',
          status: data.status || 1,
          createdAt: data.createdAt ? this.formatDate(data.createdAt) : ''
        };
        this.setData({ userInfo });
      }
    }).catch(err => {
      console.error('Failed to load user info:', err);
    });
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`userInfo.${field}`]: e.detail.value });
  },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.uploadAvatar(tempFilePath);
      }
    });
  },

  async uploadAvatar(filePath) {
    const token = wx.getStorageSync('token');
    
    wx.showLoading({ title: '上传中...' });
    
    try {
      const uploadRes = await new Promise((resolve, reject) => {
        wx.uploadFile({
          url: config.UPLOAD_IMAGE,
          filePath: filePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${token}`
          },
          success: (res) => {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve(data.data.url);
              } else {
                reject(new Error(data.message || '上传失败'));
              }
            } catch (e) {
              reject(e);
            }
          },
          fail: reject
        });
      });
      
      const avatarUrl = app.getImageUrl(uploadRes);
      this.setData({ 'userInfo.avatar': avatarUrl, 'userInfo._avatarPath': uploadRes });
      
      wx.hideLoading();
      wx.showToast({ title: '头像已更新', icon: 'success' });
    } catch (err) {
      wx.hideLoading();
      console.error('Upload avatar error:', err);
      wx.showToast({ title: err.message || '上传失败', icon: 'none' });
    }
  },

  async saveProfile() {
    const { userInfo } = this.data;
    
    if (!userInfo.nickname || userInfo.nickname.trim() === '') {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }
    
    this.setData({ loading: true });
    
    try {
      const updateData = {
        nickname: userInfo.nickname.trim()
      };
      
      if (userInfo.realName !== undefined) {
        updateData.realName = userInfo.realName.trim();
      }
      if (userInfo.idCard !== undefined) {
        updateData.idCard = userInfo.idCard.trim();
      }
      if (userInfo.email !== undefined) {
        updateData.email = userInfo.email.trim();
      }
      if (userInfo._avatarPath) {
        updateData.avatar = userInfo._avatarPath;
      }
      if (userInfo.deliveryAddress !== undefined) {
        updateData.deliveryAddress = userInfo.deliveryAddress.trim();
      }
      if (userInfo.deliveryName !== undefined) {
        updateData.deliveryName = userInfo.deliveryName.trim();
      }
      if (userInfo.deliveryPhone !== undefined) {
        updateData.deliveryPhone = userInfo.deliveryPhone.trim();
      }
      if (userInfo.province !== undefined) {
        updateData.province = userInfo.province.trim();
      }
      if (userInfo.city !== undefined) {
        updateData.city = userInfo.city.trim();
      }
      if (userInfo.district !== undefined) {
        updateData.district = userInfo.district.trim();
      }
      
      await app.request({
        url: config.USER_PROFILE,
        method: 'PUT',
        data: updateData
      });
      
      const updatedUserInfo = { ...userInfo };
      delete updatedUserInfo._avatarPath;
      app.globalData.userInfo = updatedUserInfo;
      wx.setStorageSync('userInfo', updatedUserInfo);
      
      wx.showToast({ title: '保存成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 1500);
    } catch (err) {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goBack() {
    wx.navigateBack();
  },

  goAddressList() {
    wx.navigateTo({ url: '/pages/user/address' });
  }
});
