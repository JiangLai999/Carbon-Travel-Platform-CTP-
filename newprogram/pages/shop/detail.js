const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    product: null,
    addresses: [],
    selectedAddress: null,
    selectedAddressId: null,
    hasAddress: false,
    showAddressModal: false,
    addressForm: {
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: ''
    }
  },

  onLoad(options) {
    if (!options.id) {
      wx.showToast({ title: '商品不存在', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.productId = options.id;
    this.loadProduct();
    this.loadAddresses();
  },

  onShow() {
    this.loadAddresses();
  },

  loadProduct() {
    if (!this.productId) {
      return;
    }
    var that = this;
    wx.request({
      url: config.API_BASE + '/shop/products/' + this.productId,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 200 && res.data.data) {
          var data = res.data.data;
          if (data.imageUrl) {
            data.imageUrl = app.getImageUrl(data.imageUrl);
          }
          that.setData({ product: data });
        } else {
          wx.showToast({ title: '商品不存在', icon: 'none' });
        }
      },
      fail: function() {
        wx.showToast({ title: '加载失败', icon: 'none' });
      }
    });
  },

  loadAddresses() {
    app.request({
      url: config.ADDRESS_LIST,
      method: 'GET'
    }).then(data => {
      const addresses = data || [];
      let selectedAddress = null;
      let selectedAddressId = null;
      
      for (const addr of addresses) {
        if (addr.isDefault === 1) {
          selectedAddress = addr.fullAddress;
          selectedAddressId = addr.id;
          break;
        }
      }
      
      if (!selectedAddress && addresses.length > 0) {
        selectedAddress = addresses[0].fullAddress;
        selectedAddressId = addresses[0].id;
      }
      
      this.setData({
        addresses,
        selectedAddress,
        selectedAddressId,
        hasAddress: addresses.length > 0
      });
    }).catch(err => {
      console.error('Failed to load addresses:', err);
    });
  },

  selectAddress() {
    const { addresses } = this.data;
    if (addresses.length === 0) {
      this.openAddressModal();
      return;
    }

    const itemList = addresses.map(a => `${a.name} ${a.phone} - ${a.fullAddress}`);
    wx.showActionSheet({
      itemList: itemList,
      success: (res) => {
        const addr = addresses[res.tapIndex];
        this.setData({
          selectedAddress: addr.fullAddress,
          selectedAddressId: addr.id
        });
        wx.showToast({ title: '已选择地址', icon: 'success' });
      }
    });
  },

  openAddressModal() {
    this.setData({
      showAddressModal: true,
      addressForm: { name: '', phone: '', province: '', city: '', district: '', detailAddress: '' }
    });
  },

  closeAddressModal() {
    this.setData({ showAddressModal: false });
  },

  onAddressInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`addressForm.${field}`]: e.detail.value });
  },

  async saveAddressForm() {
    const { addressForm } = this.data;
    
    if (!addressForm.name || !addressForm.name.trim()) {
      wx.showToast({ title: '请输入收货人', icon: 'none' });
      return;
    }
    if (!addressForm.phone || !addressForm.phone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' });
      return;
    }
    if (!addressForm.province || !addressForm.province.trim()) {
      wx.showToast({ title: '请输入省份', icon: 'none' });
      return;
    }
    if (!addressForm.city || !addressForm.city.trim()) {
      wx.showToast({ title: '请输入城市', icon: 'none' });
      return;
    }
    if (!addressForm.detailAddress || !addressForm.detailAddress.trim()) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' });
      return;
    }

    try {
      const data = await app.request({
        url: config.ADDRESS_ADD,
        method: 'POST',
        data: { ...addressForm, isDefault: 0 }
      });

      wx.showToast({ title: '添加成功', icon: 'success' });
      this.setData({ showAddressModal: false });
      this.loadAddresses();
    } catch (err) {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    }
  },

  doExchangeClick() {
    const { product, selectedAddress } = this.data;
    if (!product) return;

    if (product.stock <= 0) {
      wx.showToast({ title: '商品已售罄', icon: 'none' });
      return;
    }

    if (!selectedAddress) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认兑换',
      content: `将消耗 ${product.pointsRequired} 积分兑换 "${product.name}"\n收货地址: ${selectedAddress}`,
      success: (res) => {
        if (res.confirm) {
          this.doExchange(selectedAddress);
        }
      }
    });
  },

  doExchange(address) {
    const { product } = this.data;
    wx.showLoading({ title: '兑换中...' });
    app.request({
      url: `${config.SHOP_EXCHANGE}?productId=${this.productId}&quantity=1&address=${encodeURIComponent(address)}`,
      method: 'POST'
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '兑换成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 1500);
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || '兑换失败', icon: 'none' });
    });
  }
});
