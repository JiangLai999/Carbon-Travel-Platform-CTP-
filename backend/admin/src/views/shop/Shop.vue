<template>
  <div class="shop-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">商城管理</h1>
        <p class="page-subtitle">管理可持续商品并跟踪用户奖励兑换情况</p>
      </div>
      <el-button type="primary" class="btn-rose-light add-btn" @click="openAddDialog">
        添加商品
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="shop-tabs">
      <el-tab-pane label="商品列表" name="products">
        <div class="products-grid">
          <div v-for="product in products" :key="product.id" class="product-card">
            <div class="product-image">
              <img :src="product.imageUrl" :alt="product.name" @error="handleImageError" />
              <span :class="['status-tag', product.stock > 0 ? 'in-stock' : 'out-of-stock']">
                {{ product.stock > 0 ? '上架中' : '缺货' }}
              </span>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-points">
                <span class="token-icon">🪙</span>
                <span class="points-value">{{ product.pointsRequired || 0 }}</span>
                <span class="points-label">积分</span>
              </div>
              <div class="product-stock">
                <span class="stock-label">库存:</span>
                <span class="stock-value">{{ product.stock || 0 }}</span>
              </div>
              <div class="product-actions">
                <el-button class="btn-rose-light" size="small" @click="editProduct(product)">
                  编辑
                </el-button>
                <el-button class="btn-gray-light" size="small" @click="deleteProduct(product)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div v-if="products.length === 0" class="empty-state">
          <p>暂无商品，点击"添加商品"创建</p>
        </div>
      </el-tab-pane>

      <el-tab-pane name="redemptions">
        <template #label>
          <span class="tab-label">
            兑换申请
            <el-badge :value="pendingCount" class="tab-badge" />
          </span>
        </template>
        <div class="table-toolbar">
          <el-select v-model="orderStatusFilter" placeholder="订单状态" clearable style="width: 120px" @change="loadRedemptions">
            <el-option label="全部" value="" />
            <el-option label="待处理" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
          <el-button :icon="Refresh" @click="loadRedemptions">刷新</el-button>
        </div>
        <el-table :data="redemptions" stripe border class="redemption-table">
          <el-table-column label="订单号" width="150">
            <template #default="{ row }">
              <span class="order-no">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="用户" width="180">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="32" :src="row.userAvatar">
                  {{ (row.userName || 'U').charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ row.userName }}</span>
                  <span class="user-phone">{{ row.phone }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="product-cell">
                <span class="product-title">{{ row.productName }}</span>
                <span class="product-points-small">
                  <span class="token-icon-small">🪙</span>
                  {{ row.points }} 积分 × {{ row.quantity || 1 }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="收货信息" min-width="200">
            <template #default="{ row }">
              <div class="address-cell">
                <span>{{ row.address || '暂无地址' }}</span>
                <span v-if="row.trackingNo" class="tracking-no">快递: {{ row.trackingNo }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="日期" width="120">
            <template #default="{ row }">
              <span>{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.statusNum)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="row.statusNum === 0"
                  class="btn-rose-light"
                  size="small"
                  @click="shipOrder(row)"
                >
                  发货
                </el-button>
                <el-button
                  v-if="row.statusNum === 0"
                  class="btn-gray-light"
                  size="small"
                  @click="rejectOrder(row)"
                >
                  拒绝
                </el-button>
                <el-button
                  v-if="row.statusNum === 1"
                  class="btn-green-light"
                  size="small"
                  @click="completeOrder(row)"
                >
                  完成
                </el-button>
                <span v-if="row.statusNum === 2" class="completed-text">已完成</span>
                <span v-if="row.statusNum === 3" class="rejected-text">已拒绝</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="table-pagination">
          <el-pagination
            v-model:current-page="orderPage"
            :page-size="orderSize"
            :total="orderTotal"
            layout="total, prev, pager, next"
            @current-change="loadRedemptions"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="categories">
        <div class="table-toolbar">
          <el-button class="btn-rose-light" :icon="Plus" @click="openCategoryDialog">添加分类</el-button>
        </div>
        <el-table :data="categories" stripe border>
          <el-table-column label="分类名称" prop="name" width="150" />
          <el-table-column label="分类编码" prop="code" width="120" />
          <el-table-column label="描述" prop="description" min-width="200" />
          <el-table-column label="排序" prop="sortOrder" width="80" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <el-button class="btn-rose-light" size="small" text @click="editCategory(row)">编辑</el-button>
              <el-button class="btn-gray-light" size="small" text @click="deleteCategory(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showAddDialog" :title="isEditing ? '编辑商品' : '添加商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="upload-section">
            <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <div v-if="form.imageUrl" class="uploaded-image">
                <img :src="form.imageUrl" alt="商品图片" />
                <div class="image-actions">
                  <el-icon @click.stop="removeImage"><Delete /></el-icon>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>点击上传图片</span>
              </div>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最大 5MB</div>
          </div>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="cat in categories.filter(c => c.status === 1)" 
              :key="cat.code" 
              :label="cat.name" 
              :value="cat.code" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="积分价格">
          <el-input-number v-model="form.pointsRequired" :min="0" />
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="低碳标签">
          <el-input v-model="form.carbonLabel" placeholder="如: 低碳环保" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusActive" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button class="btn-rose-solid" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showShipDialog" title="发货" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="shipForm.receiver" disabled />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="shipForm.address" disabled type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShipDialog = false">取消</el-button>
        <el-button class="btn-rose-solid" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCategoryDialog" :title="isEditingCategory ? '编辑分类' : '添加分类'" width="500px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="categoryForm.code" placeholder="请输入分类编码（英文）" :disabled="isEditingCategory" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="2" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="categoryForm.statusActive" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCategoryDialog = false">取消</el-button>
        <el-button class="btn-rose-solid" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Delete } from '@element-plus/icons-vue'
import api, { getImageUrl } from '../../api'

const uploadUrl = '/api/upload/image'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const activeTab = ref('products')
const showAddDialog = ref(false)
const showShipDialog = ref(false)
const isEditing = ref(false)
const loading = ref(false)

const products = ref([])

const redemptions = ref([])
const orderPage = ref(1)
const orderSize = ref(10)
const orderTotal = ref(0)
const orderStatusFilter = ref('')

const form = ref({
  id: null,
  name: '',
  imageUrl: '',
  description: '',
  category: 'other',
  pointsRequired: 100,
  stock: 10,
  carbonLabel: '',
  statusActive: true,
  status: 1
})

const shipForm = ref({
  id: null,
  receiver: '',
  address: '',
  trackingNo: ''
})

// 分类管理相关
const categories = ref([])
const showCategoryDialog = ref(false)
const isEditingCategory = ref(false)
const categoryForm = ref({
  id: null,
  name: '',
  code: '',
  description: '',
  sortOrder: 99,
  statusActive: true
})

const pendingCount = computed(() => {
  return redemptions.value.filter(r => r.statusNum === 0).length
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/300x200?text=暂无图片'
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.value.imageUrl = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const removeImage = () => {
  form.value.imageUrl = ''
}

const openAddDialog = () => {
  isEditing.value = false
  form.value = {
    id: null,
    name: '',
    imageUrl: '',
    description: '',
    category: 'other',
    pointsRequired: 100,
    stock: 10,
    carbonLabel: '',
    statusActive: true,
    status: 1
  }
  showAddDialog.value = true
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/products')
    if (Array.isArray(res)) {
      products.value = res.map(p => ({
        ...p,
        imageUrl: getImageUrl(p.imageUrl)
      }))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

const editProduct = (product) => {
  isEditing.value = true
  form.value = {
    id: product.id,
    name: product.name || '',
    imageUrl: product.imageUrl || '',
    description: product.description || '',
    category: product.category || 'other',
    pointsRequired: product.pointsRequired || 0,
    stock: product.stock || 0,
    carbonLabel: product.carbonLabel || '',
    statusActive: product.status === 1,
    status: product.status || 1
  }
  showAddDialog.value = true
}

const deleteProduct = async (product) => {
  try {
    await ElMessageBox.confirm('确认删除该商品？删除后不可恢复。', '删除确认', { type: 'warning' })
    await api.delete(`/admin/products/${product.id}`)
    ElMessage.success('商品已删除')
    loadProducts()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

const closeDialog = () => {
  showAddDialog.value = false
  isEditing.value = false
}

const saveProduct = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  try {
    const data = {
      name: form.value.name,
      description: form.value.description,
      imageUrl: form.value.imageUrl,
      category: form.value.category,
      pointsRequired: form.value.pointsRequired,
      stock: form.value.stock,
      carbonLabel: form.value.carbonLabel,
      status: form.value.statusActive ? 1 : 0
    }
    if (isEditing.value) {
      await api.put(`/admin/products/${form.value.id}`, data)
      ElMessage.success('商品更新成功')
    } else {
      await api.post('/admin/products', data)
      ElMessage.success('商品添加成功')
    }
    closeDialog()
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

const loadRedemptions = async () => {
  try {
    const params = { page: orderPage.value, size: orderSize.value }
    if (orderStatusFilter.value !== '') {
      params.status = orderStatusFilter.value
    }
    const res = await api.get('/admin/orders', { params })
    if (res.records) {
      redemptions.value = res.records.map(o => ({
        id: o.id,
        orderNo: o.orderNo || `ORD${o.id}`,
        userName: o.userName || `用户${o.userId}`,
        userAvatar: o.avatar || '',
        phone: o.phone || '',
        productName: o.productName || '商品',
        quantity: o.quantity || 1,
        points: o.pointsSpent || 0,
        status: o.status === 0 ? '待处理' : o.status === 1 ? '已发货' : o.status === 2 ? '已完成' : '已取消',
        statusNum: o.status || 0,
        address: o.deliveryAddress,
        trackingNo: o.deliveryNo,
        createdAt: o.createdAt
      }))
      orderTotal.value = res.total || 0
    }
  } catch (e) {
    console.error('Failed to load orders:', e)
  }
}

const shipOrder = (row) => {
  shipForm.value = {
    id: row.id,
    receiver: row.userName,
    address: row.address || '暂无地址',
    trackingNo: ''
  }
  showShipDialog.value = true
}

const confirmShip = async () => {
  if (!shipForm.value.trackingNo) {
    ElMessage.warning('请输入快递单号')
    return
  }
  try {
    await api.put(`/admin/orders/${shipForm.value.id}/status`, null, {
      params: { status: 1, trackingNo: shipForm.value.trackingNo }
    })
    ElMessage.success('订单已发货')
    showShipDialog.value = false
    loadRedemptions()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const rejectOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该兑换申请？积分将返还给用户。', '拒绝确认', { type: 'warning' })
    await api.put(`/admin/orders/${row.id}/status`, null, {
      params: { status: 3 }
    })
    ElMessage.success('订单已拒绝')
    loadRedemptions()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const completeOrder = async (row) => {
  try {
    await api.put(`/admin/orders/${row.id}/status`, null, {
      params: { status: 2 }
    })
    ElMessage.success('订单已完成')
    loadRedemptions()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

// 分类管理方法
const loadCategories = async () => {
  try {
    const res = await api.get('/admin/product-categories')
    categories.value = res || []
  } catch (e) {
    console.error('Failed to load categories:', e)
  }
}

const openCategoryDialog = () => {
  isEditingCategory.value = false
  categoryForm.value = {
    id: null,
    name: '',
    code: '',
    description: '',
    sortOrder: 99,
    statusActive: true
  }
  showCategoryDialog.value = true
}

const editCategory = (row) => {
  isEditingCategory.value = true
  categoryForm.value = {
    id: row.id,
    name: row.name || '',
    code: row.code || '',
    description: row.description || '',
    sortOrder: row.sortOrder || 99,
    statusActive: row.status === 1
  }
  showCategoryDialog.value = true
}

const saveCategory = async () => {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (!categoryForm.value.code) {
    ElMessage.warning('请输入分类编码')
    return
  }
  try {
    const data = {
      name: categoryForm.value.name,
      code: categoryForm.value.code,
      description: categoryForm.value.description,
      sortOrder: categoryForm.value.sortOrder,
      status: categoryForm.value.statusActive ? 1 : 0
    }
    if (isEditingCategory.value) {
      await api.put(`/admin/product-categories/${categoryForm.value.id}`, data)
      ElMessage.success('分类更新成功')
    } else {
      await api.post('/admin/product-categories', data)
      ElMessage.success('分类添加成功')
    }
    showCategoryDialog.value = false
    loadCategories()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

const deleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该分类？', '删除确认', { type: 'warning' })
    await api.delete(`/admin/product-categories/${row.id}`)
    ElMessage.success('分类已删除')
    loadCategories()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadProducts()
  loadRedemptions()
  loadCategories()
})
</script>

<style scoped>
.shop-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.add-btn {
  background: #ec5b13;
  border-color: #ec5b13;
}

/* 商城管理 - 玫红色系浅色按钮 */
:deep(.btn-rose-light) {
  color: #e11d48 !important;
  background: #fff1f2 !important;
  border: 1px solid #fecdd3 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-rose-light:hover) {
  background: #ffe4e6 !important;
  border-color: #fda4af !important;
}
:deep(.btn-rose-solid) {
  background: #e11d48 !important;
  border-color: #e11d48 !important;
  color: #fff !important;
}
:deep(.btn-rose-solid:hover) {
  background: #be123c !important;
  border-color: #be123c !important;
}
:deep(.btn-gray-light) {
  color: #4b5563 !important;
  background: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-gray-light:hover) {
  background: #f3f4f6 !important;
  border-color: #d1d5db !important;
}
:deep(.btn-green-light) {
  color: #16a34a !important;
  background: #f0fdf4 !important;
  border: 1px solid #bbf7d0 !important;
  border-radius: 6px;
  font-weight: 500;
}
:deep(.btn-green-light:hover) {
  background: #dcfce7 !important;
  border-color: #86efac !important;
}

.add-btn:hover,
.add-btn:focus {
  background: #d14d0b;
  border-color: #d14d0b;
}

.shop-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.shop-tabs :deep(.el-tabs__item.is-active) {
  color: #ec5b13;
}

.shop-tabs :deep(.el-tabs__active-bar) {
  background-color: #ec5b13;
}

.shop-tabs :deep(.el-tabs__item:hover) {
  color: #ec5b13;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-badge :deep(.el-badge__content) {
  background: #ec5b13;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 16px 0;
}

@media (max-width: 1400px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1000px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.in-stock {
  background: rgba(82, 196, 26, 0.9);
  color: #fff;
}

.status-tag.out-of-stock {
  background: rgba(255, 77, 79, 0.9);
  color: #fff;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.product-points {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.token-icon {
  font-size: 18px;
}

.points-value {
  font-size: 20px;
  font-weight: 700;
  color: #ec5b13;
}

.points-label {
  font-size: 14px;
  color: #666;
}

.product-stock {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.stock-label {
  color: #999;
}

.stock-value {
  font-weight: 600;
  color: #333;
  margin-left: 4px;
}

.edit-btn {
  width: 100%;
  background: #ec5b13;
  border-color: #ec5b13;
}

.edit-btn:hover,
.edit-btn:focus {
  background: #d14d0b;
  border-color: #d14d0b;
}

.redemption-table {
  width: 100%;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.product-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-title {
  font-weight: 500;
  color: #333;
}

.product-points-small {
  font-size: 12px;
  color: #666;
}

.token-icon-small {
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .el-button--primary {
  background: #ec5b13;
  border-color: #ec5b13;
}

.action-buttons .el-button--primary:hover,
.action-buttons .el-button--primary:focus {
  background: #d14d0b;
  border-color: #d14d0b;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.order-no {
  font-family: monospace;
  color: #666;
  font-size: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-phone {
  font-size: 12px;
  color: #999;
}

.address-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.tracking-no {
  font-size: 12px;
  color: #ec5b13;
}

.table-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.product-actions {
  display: flex;
  gap: 8px;
}

.product-actions .el-button {
  flex: 1;
}

.image-preview {
  margin-top: 10px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  overflow: hidden;
  max-height: 120px;
}

.image-preview img {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
}

.upload-section {
  width: 100%;
}

.image-uploader {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #ec5b13;
}

.uploaded-image {
  width: 100%;
  height: 120px;
  position: relative;
}

.uploaded-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.uploaded-image:hover .image-actions {
  opacity: 1;
}

.image-actions .el-icon {
  font-size: 24px;
  color: #fff;
  cursor: pointer;
}

.upload-placeholder {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.completed-text,
.rejected-text {
  font-size: 12px;
  color: #999;
}
</style>
