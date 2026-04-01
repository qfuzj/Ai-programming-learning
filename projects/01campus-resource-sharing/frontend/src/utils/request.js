import axios from 'axios';
import { getToken, clearAuth } from './auth';

const normalizeApiBaseUrl = (raw) => {
  const fallback = 'http://localhost:8080/api';
  if (!raw || typeof raw !== 'string') {
    return fallback;
  }
  const trimmed = raw.trim().replace(/\/$/, '');
  if (trimmed.endsWith('/api')) {
    return trimmed;
  }
  return `${trimmed}/api`;
};

const request = axios.create({
  baseURL: normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL),
  timeout: 10000
});

request.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const payload = response.data;

    // 兼容后端统一 Result 结构：{ code, message, data }
    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code === 200) {
        return payload.data;
      }
      return Promise.reject(new Error(payload.message || '请求失败'));
    }

    // 兼容非 Result 结构
    return payload;
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      if (window.location.pathname.startsWith('/admin')) {
        window.location.href = '/admin/login';
      } else {
        window.location.href = '/login';
      }
    }
    const backendMessage = error?.response?.data?.message;
    return Promise.reject(new Error(backendMessage || error.message || '网络错误'));
  }
);

export default request;
