const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    selectedTab: 0,
    selectedSectionId: 0,
    sections: [],
    posts: [],
    searchKeyword: ''
  },

  onLoad() {
    this.loadSections();
  },

  onShow() {
    if (typeof this.getTabBar === 'function') {
      this.getTabBar().setData({ selected: 3 });
    }
    this.loadPosts();
  },

  loadSections() {
    app.request({
      url: config.FORUM_SECTIONS,
      method: 'GET'
    }).then(data => {
      const sections = data || [];
      this.setData({ sections });
      if (sections.length > 0) {
        this.setData({ selectedSectionId: sections[0].id });
      }
      this.loadPosts();
    }).catch(err => {
      console.error('Failed to load sections:', err);
    });
  },

  selectTab(e) {
    const idx = parseInt(e.currentTarget.dataset.idx);
    const sectionId = this.data.sections[idx]?.id || 0;
    this.setData({ 
      selectedTab: idx,
      selectedSectionId: sectionId
    });
    this.loadPosts();
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  loadPosts() {
    const { selectedSectionId, searchKeyword } = this.data;
    let url = config.FORUM_POSTS + '?page=1&size=20';
    if (selectedSectionId > 0) {
      url += `&sectionId=${selectedSectionId}`;
    }
    if (searchKeyword) {
      url += `&keyword=${encodeURIComponent(searchKeyword)}`;
    }

    app.request({
      url,
      method: 'GET'
    }).then(data => {
      const posts = (data?.records || data || []).map(p => {
        let images = [];
        const rawImages = p.images || p.imageUrls || p.imgs;

        if (rawImages) {
          if (Array.isArray(rawImages)) {
            images = rawImages;
          } else {
            try {
              images = JSON.parse(rawImages);
              if (!Array.isArray(images)) {
                images = images ? [images] : [];
              }
            } catch (e) {
              const rawText = String(rawImages).trim();
              images = rawText ? (rawText.indexOf(',') > -1 ? rawText.split(',') : [rawText]) : [];
            }
          }
        }

        const normalizedImages = images
          .filter(img => !!img)
          .map(img => String(img).trim())
          .filter(img => img.length > 0)
          .map(img => app.getImageUrl(img));

        return {
          ...p,
          authorAvatar: app.getImageUrl(p.authorAvatar || p.userAvatar),
          images: normalizedImages,
          firstImage: normalizedImages.length > 0 ? normalizedImages[0] : ''
        };
      });
      this.setData({ posts });
    }).catch(err => {
      console.error('Failed to load posts:', err);
    });
  },

  onSearch() {
    this.loadPosts();
  },

  goPostDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/forum/detail?id=${id}` });
  },

  goPost() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: '/pages/forum/create' });
  }
});
