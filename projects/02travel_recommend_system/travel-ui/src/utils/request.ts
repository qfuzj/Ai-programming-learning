import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import type { ApiResponse } from "@/types/api";
import router from "@/router";
import { ROUTE_PATHS } from "@/router/constants";

// 是否正在刷新token
let isRefreshing = false;
// 刷新期间等待的请求队列
let pendingRequests: Array<(token: string) => void> = [];

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000,
});

const TOKEN_KEY = "travel_token";
const REFRESH_TOKEN_KEY = "travel_refresh_token";
const ROLE_KEY = "travel_role";

// 请求拦截器
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const payload = response.data as ApiResponse<unknown>;

    // 非标准响应格式直接返回（如文件下载)
    if (!payload || typeof payload.code !== "number") {
      return response.data;
    }

    // 成功
    if (payload.code === 200) {
      return payload.data;
    }

    // token 过期，尝试刷新
    if (payload.code === 401) {
      return handleTokenExpired(response.config);
    }

    // 其他业务错误
    ElMessage.error(payload.message || "请求失败");
    return Promise.reject(new Error(payload.message || "Request failed"));
  },
  (error: AxiosError) => {
    if (error.response?.status === 401 && error.config) {
      return handleTokenExpired(error.config);
    }
    ElMessage.error(error.message || "网络异常，请稍后重试");
    return Promise.reject(error);
  }
);

/**
 * 处理 Token 过期：先尝试刷新，失败再调登录页
 */
async function handleTokenExpired(config: InternalAxiosRequestConfig): Promise<unknown> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
  const role = localStorage.getItem(ROLE_KEY);

  if (!refreshToken) {
    redirectToLogin();
    return new Promise(() => {});
  }

  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      pendingRequests.push((newToken: string) => {
        config.headers.Authorization = `Bearer ${newToken}`;
        request(config).then(resolve).catch(reject); // 失败也能正确reject
      });
    });
  }

  isRefreshing = true;
  try {
    const refreshUrl = getRefreshUrl(role);

    const response = await axios.post<ApiResponse<{ accessToken: string }>>(refreshUrl, {
      refreshToken,
    });

    const newToken = response.data?.data?.accessToken;
    if (!newToken) {
      throw new Error("刷新令牌响应格式错误");
    }
    localStorage.setItem(TOKEN_KEY, newToken);

    pendingRequests.forEach((cb) => cb(newToken));
    pendingRequests = [];

    config.headers.Authorization = `Bearer ${newToken}`;
    return request(config);
  } catch {
    pendingRequests = [];
    const { useUserStore } = await import("@/store");
    const userStore = useUserStore();
    userStore.clearAuth();
    redirectToLogin();
    ElMessage.warning("登录已过期，请重新登录");
    return new Promise(() => {});
  } finally {
    isRefreshing = false;
  }
}

function redirectToLogin(): void {
  const role = localStorage.getItem(ROLE_KEY);
  const loginPath = role === "ADMIN" ? ROUTE_PATHS.ADMIN_LOGIN : ROUTE_PATHS.USER_LOGIN;
  void router.push({
    path: loginPath,
    query: { redirect: router.currentRoute.value.fullPath },
  });
}

function getRefreshUrl(role: string | null): string {
  const base = import.meta.env.VITE_APP_BASE_API;
  return role === "ADMIN"
    ? `${base}/api/admin/auth/refresh-token`
    : `${base}/api/user/auth/refresh-token`;
}

export default request;
