const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    activeTab: 'all',
    tabs: [
      { id: 'all', name: '全部' },
      { id: 'unread', name: '未读' },
      { id: 'interaction', name: '互动' },
      { id: 'review', name: '审核' },
      { id: 'system', name: '系统' }
    ],
    messages: [],
    iconMap: {
      'system': '🔔',
      'travel': '🚴',
      'exchange': '🎁',
      'activity': '📋',
      'forum_like': '❤️',
      'forum_comment': '💬',
      'forum_approved': '✅',
      'forum_rejected': '❌',
      'points_earned': '💰'
    },
    // 分类对应的类型
    categoryTypes: {
      'interaction': ['forum_like', 'forum_comment'],
      'review': ['forum_approved', 'forum_rejected', 'travel', 'activity'],
      'system': ['system', 'exchange', 'points_earned']
    }
  },

  onLoad() {
    this.loadMessages();
  },

  onShow() {
    this.loadMessages();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab });
    this.loadMessages();
  },

  loadMessages() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    const { activeTab } = this.data;
    let url = config.MESSAGES + '?page=1&size=50';
    
    if (activeTab === 'unread') {
      url += '&isRead=0';
    }
    // 其他分类在前端过滤

    app.request({ url, method: 'GET' }).then(data => {
      let messages = (data?.records || data || []).map(m => ({
        ...m,
        icon: this.getIcon(m.type),
        timeStr: this.formatTime(m.createdAt)
      }));
      
      // 前端过滤分类
      if (activeTab !== 'all' && activeTab !== 'unread') {
        const types = this.data.categoryTypes[activeTab] || [];
        messages = messages.filter(m => types.includes(m.type));
      }
      
      this.setData({ messages });
    }).catch(err => {
      console.error('Failed to load messages:', err);
    });
  },

  getIcon(type) {
    return this.data.iconMap[type] || '🔔';
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

  readMessage(e) {
    const id = e.currentTarget.dataset.id;
    const messages = this.data.messages;
    const msg = messages.find(m => m.id === id);
    if (!msg || msg.isRead === 1) return;

    app.request({
      url: config.MESSAGE_READ(id),
      method: 'PUT'
    }).then(() => {
      const idx = messages.findIndex(m => m.id === id);
      messages[idx].isRead = 1;
      this.setData({ messages });
    }).catch(() => {});
  },

  markAllRead() {
    wx.showModal({
      title: '提示',
      content: '确定将所有消息标记为已读？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: config.MESSAGES_READ_ALL,
            method: 'PUT'
          }).then(() => {
            const messages = this.data.messages.map(m => ({ ...m, isRead: 1 }));
            this.setData({ messages });
            wx.showToast({ title: '全部已读', icon: 'success' });
          }).catch(() => {});
        }
      }
    });
  },

  goBack() {
    wx.navigateBack({ delta: 1 });
  }
});
