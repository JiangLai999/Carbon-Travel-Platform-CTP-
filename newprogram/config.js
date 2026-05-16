// API 配置
const API_BASE = 'http://localhost:8080/api';

module.exports = {
  API_BASE,

  // Auth
  AUTH_LOGIN: `${API_BASE}/auth/login`,
  AUTH_REGISTER: `${API_BASE}/auth/register`,
  AUTH_SEND_CODE: `${API_BASE}/auth/send-code`,
  AUTH_RESET_PASSWORD: `${API_BASE}/auth/reset-password`,

  // User
  USER_PROFILE: `${API_BASE}/user/profile`,
  USER_STATS: `${API_BASE}/user/stats`,
  USER_POINTS: `${API_BASE}/user/points`,
  USER_POINTS_DETAILS: `${API_BASE}/user/points/details`,
  USER_TRAVEL_MODES: `${API_BASE}/user/travel-modes`,
  USER_ANNOUNCEMENTS: `${API_BASE}/user/announcements`,
  USER_ANNOUNCEMENT_DETAIL: (id) => `${API_BASE}/user/announcements/${id}`,
  USER_JOINED_ACTIVITIES: `${API_BASE}/user/activities/joined`,
  USER_ORDERS: `${API_BASE}/user/orders`,
  USER_RANK: `${API_BASE}/user/rank`,

  // Travel
  TRAVEL_RECORD: `${API_BASE}/travel/record`,
  TRAVEL_RECORDS: `${API_BASE}/travel/records`,
  TRAVEL_RECORD_DETAIL: (id) => `${API_BASE}/travel/records/${id}`,
  TRAVEL_TRACK: `${API_BASE}/travel/track`,
  TRAVEL_STATS: `${API_BASE}/travel/stats`,
  USER_TRAVEL_STATS: `${API_BASE}/travel/user-stats`,

  // Shop
  SHOP_CATEGORIES: `${API_BASE}/shop/categories`,
  SHOP_PRODUCTS: `${API_BASE}/shop/products`,
  SHOP_PRODUCT_DETAIL: (id) => `${API_BASE}/shop/products/${id}`,
  SHOP_EXCHANGE: `${API_BASE}/shop/exchange`,
  SHOP_ORDERS: `${API_BASE}/shop/orders`,

  // Forum
  FORUM_POSTS: `${API_BASE}/forum/posts`,
  FORUM_SECTIONS: `${API_BASE}/forum/sections`,
  FORUM_POST_DETAIL: (id) => `${API_BASE}/forum/posts/${id}`,
  FORUM_POST_COMMENTS: (id) => `${API_BASE}/forum/posts/${id}/comments`,
  FORUM_MY_POSTS: `${API_BASE}/forum/my-posts`,

  // Activity
  ACTIVITY_LIST: `${API_BASE}/activities`,
  ACTIVITY_JOIN: (id) => `${API_BASE}/activities/${id}/join`,
  ACTIVITY_PARTICIPATION: `${API_BASE}/activities/participation`,

  // Messages
  MESSAGES: `${API_BASE}/messages`,
  MESSAGE_READ: (id) => `${API_BASE}/messages/${id}/read`,
  MESSAGES_READ_ALL: `${API_BASE}/messages/read-all`,
  MESSAGES_UNREAD_COUNT: `${API_BASE}/messages/unread-count`,

  // Upload
  UPLOAD_IMAGE: `${API_BASE}/upload/image`,
  UPLOAD_IMAGES: `${API_BASE}/upload/images`,

  // Address
  ADDRESS_LIST: `${API_BASE}/addresses`,
  ADDRESS_ADD: `${API_BASE}/addresses`,
  ADDRESS_DEFAULT: `${API_BASE}/addresses/default`
};
