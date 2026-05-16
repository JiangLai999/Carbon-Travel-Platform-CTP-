const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    totalPoints: 0,
    activeFilter: 'all',
    filters: [
      { id: 'all', name: '全部' },
      { id: 'income', name: '收入' },
      { id: 'expense', name: '支出' }
    ],
    records: [],
    filteredRecords: []
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  loadData() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.loadBalance();
      this.loadRecords();
    }
  },

  loadBalance() {
    app.request({
      url: config.USER_POINTS,
      method: 'GET'
    }).then(data => {
      this.setData({ totalPoints: Math.floor(data?.availablePoints || 0) });
    }).catch(err => {
      console.error('Failed to load balance:', err);
    });
  },

  loadRecords() {
    app.request({
      url: config.USER_POINTS_DETAILS + '?page=1&size=50',
      method: 'GET'
    }).then(data => {
      const records = (data?.records || data || []).map(r => ({
        id: r.id,
        title: r.description || this.getTypeName(r.type),
        points: Math.abs(r.points || 0),
        type: (r.points || 0) >= 0 ? 'income' : 'expense',
        typeName: this.getTypeName(r.type),
        timeStr: this.formatTime(r.createdAt)
      }));
      this.setData({ records });
      this.filterRecords();
    }).catch(err => {
      console.error('Failed to load records:', err);
    });
  },

  getTypeName(type) {
    const map = {
      'travel': '出行奖励',
      'exchange': '积分兑换',
      'activity': '活动奖励'
    };
    return map[type] || '积分变动';
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    return `${month}-${day} ${hour}:${minute}`;
  },

  selectFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    this.setData({ activeFilter: filter });
    this.filterRecords();
  },

  filterRecords() {
    const { records, activeFilter } = this.data;
    let filtered;
    if (activeFilter === 'income') {
      filtered = records.filter(r => r.type === 'income');
    } else if (activeFilter === 'expense') {
      filtered = records.filter(r => r.type === 'expense');
    } else {
      filtered = records;
    }
    this.setData({ filteredRecords: filtered });
  }
});
