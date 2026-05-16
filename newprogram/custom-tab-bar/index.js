Component({
  data: {
    selected: 0,
    color: "#6b7280",
    selectedColor: "#10b981",
    list: [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        iconPath: "/assets/icons/home.png",
        selectedIconPath: "/assets/icons/home_active.png"
      },
      {
        pagePath: "/pages/travel/travel",
        text: "出行",
        iconPath: "/assets/icons/travel.png",
        selectedIconPath: "/assets/icons/travel_active.png"
      },
      {
        pagePath: "/pages/shop/shop",
        text: "商城",
        iconPath: "/assets/icons/shop.png",
        selectedIconPath: "/assets/icons/shop_active.png"
      },
      {
        pagePath: "/pages/forum/forum",
        text: "论坛",
        iconPath: "/assets/icons/forum.png",
        selectedIconPath: "/assets/icons/forum_active.png"
      },
      {
        pagePath: "/pages/user/user",
        text: "我的",
        iconPath: "/assets/icons/user.png",
        selectedIconPath: "/assets/icons/user_active.png"
      }
    ]
  },

  attached() {
    setTimeout(() => {
      this.updateSelectedStatus();
    }, 100);
  },

  methods: {
    switchTab(e) {
      const index = e.currentTarget.dataset.index;
      const pagePath = this.data.list[index].pagePath;
      
      wx.switchTab({ url: pagePath });
    },

    updateSelectedStatus() {
      try {
        const pages = getCurrentPages();
        if (!pages || pages.length === 0) return;
        const currentPage = pages[pages.length - 1];
        if (!currentPage || !currentPage.route) return;
        const route = '/' + currentPage.route;
        
        const selected = this.data.list.findIndex(item => item.pagePath === route);
        if (selected !== -1 && selected !== this.data.selected) {
          this.setData({ selected });
        }
      } catch (e) {
        console.log('updateSelectedStatus error:', e);
      }
    }
  }
});
