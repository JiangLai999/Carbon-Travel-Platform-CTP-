const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    posts: [],
    loading: false
  },

  onLoad() {
    this.loadMyPosts();
  },

  onShow() {
    this.loadMyPosts();
  },

  loadMyPosts() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    app.request({
      url: config.FORUM_MY_POSTS + '?page=1&size=50',
      method: 'GET'
    }).then(data => {
      const posts = (data?.records || data || []).map(p => ({
        ...p,
        timeStr: this.formatTime(p.createdAt)
      }));
      this.setData({ posts, loading: false });
    }).catch(err => {
      console.error('Failed to load posts:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}-${day}`;
  },

  goPostDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/forum/detail?id=${id}` });
  },

  goCreatePost() {
    wx.navigateTo({ url: '/pages/forum/create' });
  },

  deletePost(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这篇帖子吗？',
      success: (res) => {
        if (res.confirm) {
          this.doDeletePost(id);
        }
      }
    });
  },

  doDeletePost(id) {
    wx.showLoading({ title: '删除中...' });
    app.request({
      url: config.FORUM_POST_DETAIL(id),
      method: 'DELETE'
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '删除成功', icon: 'success' });
      this.loadMyPosts();
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '删除失败', icon: 'none' });
    });
  },

  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
