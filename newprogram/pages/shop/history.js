var app = getApp();
var config = require('../../config.js');

Page({
  data: {
    orders: [],
    filteredOrders: [],
    currentTab: 0
  },

  onLoad: function() {
    this.loadOrders();
  },

  onShow: function() {
    this.loadOrders();
  },

  loadOrders: function() {
    var that = this;
    wx.showLoading({ title: '加载中' });
    
    wx.request({
      url: config.API_BASE + '/user/orders',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
      success: function(res) {
        wx.hideLoading();
        if (res.data && res.data.code === 200) {
          var orders = [];
          if (res.data.data && res.data.data.records) {
            orders = res.data.data.records.map(function(order) {
              if (order.createdAt) {
                var date = new Date(order.createdAt);
                order.createdAt = date.getFullYear() + '-' + 
                  (date.getMonth() + 1).toString().padStart(2, '0') + '-' + 
                  date.getDate().toString().padStart(2, '0') + ' ' + 
                  date.getHours().toString().padStart(2, '0') + ':' + 
                  date.getMinutes().toString().padStart(2, '0');
              }
              return order;
            });
          }
          that.setData({ orders: orders });
          that.filterOrders();
        } else {
          that.setData({ orders: [], filteredOrders: [] });
        }
      },
      fail: function() {
        wx.hideLoading();
        that.setData({ orders: [], filteredOrders: [] });
      }
    });
  },

  switchTab: function(e) {
    var tab = parseInt(e.currentTarget.dataset.tab);
    this.setData({ currentTab: tab });
    this.filterOrders();
  },

  filterOrders: function() {
    var orders = this.data.orders;
    var tab = this.data.currentTab;
    var filtered;
    
    if (tab === 0) {
      filtered = orders;
    } else if (tab === 1) {
      filtered = orders.filter(function(o) { return o.status === 0; });
    } else {
      filtered = orders.filter(function(o) { return o.status === 1 || o.status === 2; });
    }
    
    this.setData({ filteredOrders: filtered });
  },

  viewDetail: function(e) {
    var id = e.currentTarget.dataset.id;
    if (id) {
      wx.navigateTo({ url: '/pages/shop/order-detail?id=' + id });
    }
  }
});