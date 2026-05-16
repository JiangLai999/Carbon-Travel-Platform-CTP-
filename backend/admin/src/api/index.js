import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => {
    const responseData = res.data
    if (responseData && responseData.code === 200) {
      return responseData.data
    }
    const error = new Error(responseData?.message || '请求失败')
    error.response = { data: responseData }
    return Promise.reject(error)
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      if (!import.meta.env.DEV) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

api.upload = async (file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  
  const config = {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }
  
  if (onProgress) {
    config.onUploadProgress = (progressEvent) => {
      const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      onProgress(percent)
    }
  }
  
  const res = await axios.post('/api/upload/image', formData, config)
  if (res.data && res.data.code === 200) {
    return res.data.data
  }
  throw new Error(res.data?.message || '上传失败')
}

// 处理图片URL，将相对路径转换为完整URL
export function getImageUrl(imageUrl) {
  if (!imageUrl) return ''
  // 已经是完整URL
  if (imageUrl.startsWith('http')) return imageUrl
  // 静态资源通过 /api/uploads 访问
  if (imageUrl.startsWith('/uploads/')) {
    return '/api' + imageUrl
  }
  // 其他相对路径，拼接 /api 前缀
  if (imageUrl.startsWith('/')) {
    return '/api' + imageUrl
  }
  return '/api/' + imageUrl
}

export default api
