const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    participationId: '',
    activityId: '',
    activity: null,
    images: [],
    description: ''
  },

  onLoad(options) {
    console.log('Proof page options:', options);
    this.setData({ 
      participationId: options.participationId || '',
      activityId: options.activityId || ''
    });
    
    if (!this.data.participationId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  chooseImage() {
    wx.chooseMedia({
      count: 3 - this.data.images.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath);
        const images = [...this.data.images, ...newImages].slice(0, 3);
        this.setData({ images });
      }
    });
  },

  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.filter((_, i) => i !== index);
    this.setData({ images });
  },

  onInput(e) {
    this.setData({ description: e.detail.value });
  },

  async submit() {
    const { participationId, description, images } = this.data;
    
    if (!participationId) {
      wx.showToast({ title: '参与记录不存在', icon: 'none' });
      return;
    }
    
    if (images.length === 0 && !description.trim()) {
      wx.showToast({ title: '请上传凭证或填写说明', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认提交',
      content: '确定要提交参与凭证吗？',
      success: (res) => {
        if (res.confirm) {
          this.doSubmit();
        }
      }
    });
  },

  async doSubmit() {
    const { participationId, description, images } = this.data;
    
    wx.showLoading({ title: '提交中...' });
    
    try {
      let uploadedUrls = [];
      if (images.length > 0) {
        uploadedUrls = await this.uploadImages(images);
      }
      
      const evidence = JSON.stringify({
        description: description,
        images: uploadedUrls
      });
      
      const url = `${config.ACTIVITY_PARTICIPATION}/${participationId}/evidence`;
      
      await app.request({
        url: url,
        method: 'PUT',
        data: { evidenceUrl: evidence }
      });
      
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 1500);
    } catch (err) {
      wx.hideLoading();
      console.error('Submit error:', err);
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    }
  },

  async uploadImages(localPaths) {
    const uploadedUrls = [];
    const token = wx.getStorageSync('token');
    
    for (const path of localPaths) {
      try {
        const res = await new Promise((resolve, reject) => {
          wx.uploadFile({
            url: config.UPLOAD_IMAGE,
            filePath: path,
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
        uploadedUrls.push(res);
      } catch (err) {
        console.error('Upload error:', err);
        throw new Error('图片上传失败');
      }
    }
    
    return uploadedUrls;
  }
});
