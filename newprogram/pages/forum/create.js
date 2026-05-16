const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    title: '',
    content: '',
    sectionId: 1,
    sections: [],
    selectedSectionIdx: 0,
    images: [],
    uploading: false
  },

  onLoad() {
    this.loadSections();
  },

  loadSections() {
    app.request({
      url: config.FORUM_SECTIONS,
      method: 'GET'
    }).then(data => {
      const sections = data || [];
      this.setData({ 
        sections,
        sectionId: sections.length > 0 ? sections[0].id : 1
      });
    }).catch(err => {
      console.error('Failed to load sections:', err);
    });
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  selectSection(e) {
    const idx = parseInt(e.currentTarget.dataset.idx);
    const sectionId = this.data.sections[idx]?.id || 1;
    this.setData({ 
      selectedSectionIdx: idx,
      sectionId 
    });
  },

  chooseImage() {
    if (this.data.images.length >= 9) {
      wx.showToast({ title: '最多9张图片', icon: 'none' });
      return;
    }
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath);
        this.setData({ images: this.data.images.concat(newImages) });
      }
    });
  },

  removeImage(e) {
    const idx = e.currentTarget.dataset.idx;
    const images = this.data.images.filter((_, i) => i !== idx);
    this.setData({ images });
  },

  submitPost() {
    const { title, content, sectionId, images, uploading } = this.data;
    if (!title || !title.trim()) {
      wx.showToast({ title: '标题请填写完整', icon: 'none' });
      return;
    }
    if (!content || !content.trim()) {
      wx.showToast({ title: '内容请填写完整', icon: 'none' });
      return;
    }
    if (uploading) return;

    wx.showLoading({ title: '发布中...' });

    const submit = async () => {
      let imageUrls = [];
      if (images.length > 0) {
        this.setData({ uploading: true });
        for (const img of images) {
          const url = await this.uploadImage(img);
          console.log('上传成功, URL:', url);
          if (url) imageUrls.push(url);
        }
        this.setData({ uploading: false });
      }

      console.log('提交数据:', { title, content, sectionId, imageUrls: imageUrls });
      
      var requestData = { 
        title: title, 
        content: content, 
        sectionId: sectionId,
        images: JSON.stringify(imageUrls)
      };
      console.log('images JSON:', requestData.images);
      
      wx.request({
        url: config.API_BASE + '/forum/posts',
        method: 'POST',
        header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
        data: requestData,
        success: function(res) {
          console.log('发布响应:', res);
          wx.hideLoading();
          if (res.data && res.data.code === 200) {
            wx.showToast({ title: '发布成功', icon: 'success' });
            setTimeout(function() { wx.navigateBack(); }, 1000);
          } else {
            wx.showToast({ title: res.data ? res.data.message : '发布失败', icon: 'none' });
          }
        },
        fail: function(err) {
          wx.hideLoading();
          console.log('发布失败:', err);
          wx.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    };

    submit();
  },

  uploadImage(filePath) {
    return new Promise((resolve) => {
      wx.uploadFile({
        url: config.UPLOAD_IMAGE,
        filePath: filePath,
        name: 'file',
        header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
        success: (res) => {
          console.log('上传响应:', res);
          try {
            const data = JSON.parse(res.data);
            console.log('上传解析:', data);
            if (data.code === 200 && data.data) {
              var url = data.data.url || data.data;
              if (url && !url.startsWith('http') && !url.startsWith('/')) {
                url = '/' + url;
              }
              resolve(url);
            } else {
              console.log('上传失败，后端返回异常:', data);
              wx.showToast({ title: data.message || '图片上传失败', icon: 'none' });
              resolve(null);
            }
          } catch (e) {
            console.log('上传解析失败:', e);
            wx.showToast({ title: '图片上传响应解析失败', icon: 'none' });
            resolve(null);
          }
        },
        fail: (err) => {
          console.log('上传失败:', err);
          wx.showToast({ title: '图片上传失败', icon: 'none' });
          resolve(null);
        }
      });
    });
  }
});
