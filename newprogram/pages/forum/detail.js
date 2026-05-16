const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    post: null,
    comments: [],
    commentContent: '',
    hasLiked: false,
    images: [],
    imageList: []
  },

  onLoad(options) {
    if (!options.id) {
      wx.showToast({ title: '帖子不存在', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.postId = options.id;
    this.loadPost();
    this.loadComments();
    const likedPosts = wx.getStorageSync('likedPosts') || [];
    this.setData({ hasLiked: likedPosts.indexOf(this.postId) > -1 });
  },

  normalizeImages(imagesField) {
    if (!imagesField) {
      return [];
    }

    let images = [];

    if (Array.isArray(imagesField)) {
      images = imagesField;
    } else if (typeof imagesField === 'string') {
      const trimmed = imagesField.trim();
      if (!trimmed) {
        images = [];
      } else {
        try {
          const parsed = JSON.parse(trimmed);
          if (Array.isArray(parsed)) {
            images = parsed;
          } else if (parsed) {
            images = [parsed];
          }
        } catch (e) {
          images = trimmed.indexOf(',') > -1 ? trimmed.split(',') : [trimmed];
        }
      }
    } else {
      images = [imagesField];
    }

    return images
      .filter(img => !!img)
      .map(img => String(img).trim())
      .filter(img => img.length > 0)
      .map(img => app.getImageUrl(img));
  },

  loadPost() {
    if (!this.postId) return;

    app.request({
      url: config.FORUM_POST_DETAIL(this.postId),
      method: 'GET'
    }).then((data) => {
      const imageList = this.normalizeImages(data.images || data.imageUrls || data.imgs);
      this.setData({
        post: data,
        imageList,
        images: imageList
      });
    }).catch((err) => {
      console.error('Failed to load post:', err);
      wx.showToast({ title: err.message || '加载帖子失败', icon: 'none' });
    });
  },

  loadComments() {
    app.request({
      url: config.FORUM_POST_COMMENTS(this.postId) + '?page=1&size=50',
      method: 'GET'
    }).then(data => {
      const comments = (data?.records || data || []).map(c => ({
        ...c,
        timeStr: this.formatTime(c.createdAt)
      }));
      this.setData({ comments });
    }).catch(err => {
      console.error('Failed to load comments:', err);
    });
  },

  checkLiked() {
    const likedPosts = wx.getStorageSync('likedPosts') || [];
    this.setData({ hasLiked: likedPosts.includes(this.postId) });
  },

  previewImage(e) {
    const current = e.currentTarget.dataset.url;
    const urls = this.data.imageList || [];
    if (!current || urls.length === 0) {
      return;
    }
    wx.previewImage({
      current,
      urls
    });
  },

  onLike() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    if (this.data.hasLiked) {
      wx.showToast({ title: '已点赞过了', icon: 'none' });
      return;
    }

    app.request({
      url: config.FORUM_POST_DETAIL(this.postId) + '/like',
      method: 'POST'
    }).then(() => {
      const likedPosts = wx.getStorageSync('likedPosts') || [];
      likedPosts.push(this.postId);
      wx.setStorageSync('likedPosts', likedPosts);

      const post = this.data.post;
      post.likes = (post.likes || 0) + 1;
      this.setData({ post, hasLiked: true });

      wx.showToast({ title: '点赞成功', icon: 'success' });
    }).catch(err => {
      wx.showToast({ title: err.message || '点赞失败', icon: 'none' });
    });
  },

  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value });
  },

  submitComment() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { commentContent } = this.data;
    if (!commentContent.trim()) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    app.request({
      url: config.FORUM_POST_DETAIL(this.postId) + '/comment',
      method: 'POST',
      data: { content: commentContent }
    }).then(() => {
      wx.showToast({ title: '评论成功', icon: 'success' });
      this.setData({ commentContent: '' });
      this.loadComments();
      const post = this.data.post;
      post.commentsCount = (post.commentsCount || 0) + 1;
      this.setData({ post });
    }).catch(err => {
      wx.showToast({ title: err.message || '评论失败', icon: 'none' });
    });
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;

    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}-${day}`;
  },

  onShareAppMessage() {
    return {
      title: this.data.post?.title || '低碳出行社区',
      path: `/pages/forum/detail?id=${this.postId}`
    };
  }
});

