const app = getApp();
const config = require('../../config.js');

Page({
  data: {
    addresses: [],
    loading: false,
    showModal: false,
    isEdit: false,
    editId: null,
    addressForm: {
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: 0
    }
  },

  onLoad() {
    this.loadAddresses();
  },

  loadAddresses() {
    this.setData({ loading: true });
    app.request({
      url: config.ADDRESS_LIST,
      method: 'GET'
    }).then(data => {
      this.setData({ addresses: data || [], loading: false });
    }).catch(err => {
      this.setData({ loading: false });
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`addressForm.${field}`]: e.detail.value });
  },

  onDefaultChange(e) {
    this.setData({ 'addressForm.isDefault': e.detail.value.includes('1') ? 1 : 0 });
  },

  addAddress() {
    this.setData({
      showModal: true,
      isEdit: false,
      editId: null,
      addressForm: { name: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 }
    });
  },

  editAddress(e) {
    const id = e.currentTarget.dataset.id;
    const address = this.data.addresses.find(a => a.id === id);
    if (address) {
      this.setData({
        showModal: true,
        isEdit: true,
        editId: id,
        addressForm: {
          name: address.name,
          phone: address.phone,
          province: address.province,
          city: address.city,
          district: address.district,
          detailAddress: address.detailAddress,
          isDefault: address.isDefault || 0
        }
      });
    }
  },

  closeModal() {
    this.setData({ showModal: false });
  },

  async saveAddress() {
    const { addressForm, isEdit, editId } = this.data;

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

    const url = isEdit ? `${config.ADDRESS_ADD}/${editId}` : config.ADDRESS_ADD;
    const method = isEdit ? 'PUT' : 'POST';

    try {
      await app.request({
        url,
        method,
        data: addressForm
      });

      wx.showToast({ title: isEdit ? '修改成功' : '添加成功', icon: 'success' });
      this.setData({ showModal: false });
      this.loadAddresses();
    } catch (err) {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    }
  },

  async setDefault(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await app.request({
        url: `${config.ADDRESS_ADD}/${id}/default`,
        method: 'PUT'
      });
      wx.showToast({ title: '已设为默认', icon: 'success' });
      this.loadAddresses();
    } catch (err) {
      wx.showToast({ title: err.message || '设置失败', icon: 'none' });
    }
  },

  async deleteAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除此地址吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await app.request({
              url: `${config.ADDRESS_ADD}/${id}`,
              method: 'DELETE'
            });
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadAddresses();
          } catch (err) {
            wx.showToast({ title: err.message || '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  selectAddress(e) {
    const id = e.currentTarget.dataset.id;
    const address = this.data.addresses.find(a => a.id === id);
    if (address) {
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      if (prevPage) {
        prevPage.setData({
          selectedAddress: address.fullAddress,
          selectedAddressId: address.id
        });
      }
      wx.navigateBack();
    }
  },

  goBack() {
    wx.navigateBack();
  }
});