const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    totalPoints: 0,
    selectedCategory: 'all',
    categories: [
      { code: 'all', name: '全部' }
    ],
    products: [],
    filteredProducts: []
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    if (typeof this.getTabBar === 'function') {
      this.getTabBar().setData({ selected: 2 });
    }
    this.loadData();
  },

  loadData() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.loadPoints();
    }
    this.loadCategories();
    this.loadProducts();
  },

  loadCategories() {
    app.request({
      url: config.SHOP_CATEGORIES,
      method: 'GET'
    }).then(data => {
      const categories = [
        { code: 'all', name: '全部' },
        ...(data || []).map(c => ({ code: c.code, name: c.name }))
      ];
      this.setData({ categories });
    }).catch(err => {
      console.error('Failed to load categories:', err);
    });
  },

  loadPoints() {
    app.request({
      url: config.USER_POINTS,
      method: 'GET'
    }).then(data => {
      this.setData({ totalPoints: data?.availablePoints || 0 });
    }).catch(err => {
      console.error('Failed to load points:', err);
    });
  },

  loadProducts() {
    app.request({
      url: config.SHOP_PRODUCTS,
      method: 'GET'
    }).then(data => {
      const products = (data?.records || data || []).map(p => ({
        ...p,
        imageUrl: app.getImageUrl(p.imageUrl)
      }));
      this.setData({ products });
      this.filterProducts();
    }).catch(err => {
      console.error('Failed to load products:', err);
    });
  },

  selectCategory(e) {
    const category = e.currentTarget.dataset.category;
    this.setData({ selectedCategory: category });
    this.filterProducts();
  },

  filterProducts() {
    const { products, selectedCategory } = this.data;
    const filtered = selectedCategory === 'all'
      ? products
      : products.filter(p => p.category === selectedCategory);
    this.setData({ filteredProducts: filtered });
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/shop/detail?id=${id}` });
  },

  goExchangeHistory() {
    wx.navigateTo({ url: '/pages/shop/history' });
  }
});
