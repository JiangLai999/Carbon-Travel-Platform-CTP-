var app = getApp();
var config = require('../../config.js');

Page({
  data: {
    order: null
  },

  onLoad: function(options) {
    this.orderId = options.id;
    this.loadOrderDetail();
  },

  loadOrderDetail: function() {
    var that = this;
    wx.showLoading({ title: '加载中' });
    
    wx.request({
      url: config.API_BASE + '/user/orders?id=' + this.orderId,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
      success: function(res) {
        wx.hideLoading();
        if (res.data && res.data.code === 200 && res.data.data) {
          var order = res.data.data;
          if (order.createdAt) {
            var date = new Date(order.createdAt);
            order.createdAt = date.getFullYear() + '-' + 
              (date.getMonth() + 1).toString().padStart(2, '0') + '-' + 
              date.getDate().toString().padStart(2, '0') + ' ' + 
              date.getHours().toString().padStart(2, '0') + ':' + 
              date.getMinutes().toString().padStart(2, '0');
          }
          that.setData({ order: order });
        } else {
          wx.showToast({ title: '加载失败', icon: 'none' });
        }
      },
      fail: function() {
        wx.hideLoading();
        wx.showToast({ title: '加载失败', icon: 'none' });
      }
    });
  },

  goBack: function() {
    wx.navigateBack();
  }
});