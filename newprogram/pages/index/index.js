const app = getApp();
const config = require('../../config.js');

// 环保小贴士
const DAILY_TIPS = [
  '步行或骑行1公里，约减少0.21kg碳排放，相当于种下一棵树的1/10效果',
  '选择公共交通出行，每人每公里可减少约0.15kg碳排放',
  '每周少开一天车，一年可减少约500kg碳排放',
  '拼车出行可以分摊碳排放，还能结识新朋友',
  '短距离出行优先选择步行，既环保又健康',
  '提前规划路线，减少绕路也能降低碳排放',
  '定期保养车辆，良好的车况更省油减排'
];

// 等级配置
const LEVELS = [
  { name: '新芽', minPoints: 0, carbonTarget: 5 },
  { name: '绿叶', minPoints: 100, carbonTarget: 20 },
  { name: '银叶', minPoints: 500, carbonTarget: 50 },
  { name: '金叶', minPoints: 1000, carbonTarget: 100 },
  { name: '环保达人', minPoints: 3000, carbonTarget: 200 }
];

Page({
  data: {
    userInfo: { nickname: '', avatar: '' },
    greeting: '你好',
    userLevel: '新芽',
    monthlyCarbon: '0.0',
    totalPoints: 0,
    todayPoints: 0,
    monthRecords: 0,
    rank: 0,
    recentRecords: [],
    dailyTip: '',
    progressPercent: 0,
    nextLevelCarbon: '5.0',
    unreadCount: 0,
    modeIcons: { '步行': '🚶', '骑行': '🚴', '公交': '🚌', '地铁': '🚇', '电动车': '🛵' }
  },

  onLoad() {
    this.initGreeting();
    this.initDailyTip();
    this.loadUserInfo();
  },

  onShow() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.loadDashboard();
      this.loadRecentRecords();
    }
    if (typeof this.getTabBar === 'function') {
      this.getTabBar().setData({ selected: 0 });
    }
  },

  // 初始化问候语
  initGreeting() {
    const hour = new Date().getHours();
    let greeting = '你好';
    if (hour >= 5 && hour < 9) {
      greeting = '早上好';
    } else if (hour >= 9 && hour < 12) {
      greeting = '上午好';
    } else if (hour >= 12 && hour < 14) {
      greeting = '中午好';
    } else if (hour >= 14 && hour < 18) {
      greeting = '下午好';
    } else if (hour >= 18 && hour < 22) {
      greeting = '晚上好';
    } else {
      greeting = '夜深了';
    }
    this.setData({ greeting });
  },

  // 初始化每日小贴士
  initDailyTip() {
    const dayOfYear = Math.floor((Date.now() - new Date(new Date().getFullYear(), 0, 0)) / 86400000);
    const tipIndex = dayOfYear % DAILY_TIPS.length;
    this.setData({ dailyTip: DAILY_TIPS[tipIndex] });
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
    if (userInfo) {
      if (userInfo.avatar) {
        userInfo.avatar = app.getImageUrl(userInfo.avatar);
      }
      this.setData({ userInfo });
    }
  },

  loadDashboard() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    // 加载用户积分
    app.request({
      url: config.USER_POINTS,
      method: 'GET'
    }).then(data => {
      if (data) {
        const totalCarbon = data.totalCarbon || 0;
        const availablePoints = Math.floor(data.availablePoints || 0);
        const { level, progress, nextTarget } = this.calculateLevel(availablePoints, totalCarbon);
        
        this.setData({
          totalPoints: availablePoints,
          monthlyCarbon: totalCarbon.toFixed(1),
          userLevel: level,
          progressPercent: progress,
          nextLevelCarbon: nextTarget
        });
      }
    }).catch(err => {
      console.error('Failed to load points:', err);
    });

    // 加载出行统计
    app.request({
      url: config.USER_TRAVEL_STATS,
      method: 'GET'
    }).then(data => {
      if (data) {
        this.setData({
          monthRecords: data.monthCount || 0,
          todayPoints: data.todayPoints || 0
        });
      }
    }).catch(err => {
      console.error('Failed to load travel stats:', err);
    });
  },

  // 计算等级和进度
  calculateLevel(points, carbon) {
    let currentLevel = LEVELS[0];
    let nextLevel = LEVELS[1];
    
    for (let i = LEVELS.length - 1; i >= 0; i--) {
      if (points >= LEVELS[i].minPoints) {
        currentLevel = LEVELS[i];
        nextLevel = LEVELS[i + 1] || LEVELS[i];
        break;
      }
    }

    const progress = Math.min(100, Math.round((carbon / currentLevel.carbonTarget) * 100));
    const nextTarget = Math.max(0, (currentLevel.carbonTarget - carbon)).toFixed(1);

    return {
      level: currentLevel.name,
      progress,
      nextTarget
    };
  },

  loadRecentRecords() {
    const token = wx.getStorageSync('token');
    if (!token) return;

    app.request({
      url: config.TRAVEL_RECORDS + '?page=1&size=5',
      method: 'GET'
    }).then(data => {
      const records = data?.records || data || [];
      const recentRecords = records.map(r => {
        const modeName = r.travelModeName || r.travelMode || '出行';
        const mode = this.data.modeIcons[modeName] || '🚗';
        const distance = r.distance || 0;
        const carbon = r.carbonReduction || 0;
        const points = r.pointsEarned || 0;
        const time = this.formatTime(r.createdAt || r.travelDate);

        return {
          id: r.id,
          mode: mode,
          modeName: modeName,
          distance: distance.toFixed(1) + 'km',
          carbon: carbon.toFixed(2),
          points: points,
          time: time,
          startLocation: r.startLocation || '',
          endLocation: r.endLocation || ''
        };
      });

      this.setData({ recentRecords });
    }).catch(err => {
      console.error('Failed to load recent records:', err);
    });
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;

    // 今天
    if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      return `今天 ${hour}:${minute}`;
    }

    // 昨天
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.getDate() === yesterday.getDate() &&
      date.getMonth() === yesterday.getMonth() &&
      date.getFullYear() === yesterday.getFullYear()) {
      return '昨天';
    }

    // 更早
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}-${day}`;
  },

  goTravel() { wx.switchTab({ url: '/pages/travel/travel' }); },
  goShop() { wx.switchTab({ url: '/pages/shop/shop' }); },
  goForum() { wx.switchTab({ url: '/pages/forum/forum' }); },
  goActivity() { wx.navigateTo({ url: '/pages/activity/activity' }); },
  goPoints() { wx.navigateTo({ url: '/pages/points/points' }); },
  goMessages() { wx.navigateTo({ url: '/pages/message/message' }); }
});
