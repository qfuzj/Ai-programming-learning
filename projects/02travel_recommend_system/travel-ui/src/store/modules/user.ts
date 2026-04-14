/**
 * 用户状态：维护 token、角色和基础用户信息。
 */
import { computed, ref } from "vue";
import { defineStore } from "pinia";
import type { CurrentUser, LoginPayload, UserRole } from "@/types/auth";
import { loginAdmin, loginUser, logoutAdmin, logoutUser } from "@/api/auth";

// 从localStorage初始化，刷新页面不丢失登录状态
const TOKEN_KEY = "travel_token";
const REFRESH_TOKEN_KEY = "travel_refresh_token";
const ROLE_KEY = "travel_role";

export const useUserStore = defineStore("user", () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? "");
  const refreshToken = ref<string>(localStorage.getItem(REFRESH_TOKEN_KEY) ?? "");
  const role = ref<UserRole | "">((localStorage.getItem(ROLE_KEY) as UserRole | null) ?? "");
  // profile 不持久化，只存内存
  // 原因：profile数据量大，且可以从接口重新获取
  const profile = ref<CurrentUser | null>(null);

  // 只要token有值就认为已登录
  const isAuthenticated = computed(() => Boolean(token.value));

  function clearAuth(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    token.value = "";
    refreshToken.value = "";
    role.value = "";
    profile.value = null;
  }

  // localStorage和 ref 同步更新，保证一致性
  function persistAuth(nextToken: string, nextRefreshToken: string, nextRole: UserRole): void {
    localStorage.setItem(TOKEN_KEY, nextToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, nextRefreshToken);
    localStorage.setItem(ROLE_KEY, nextRole);
    token.value = nextToken; // 响应式更新
    refreshToken.value = nextRefreshToken;
    role.value = nextRole;
  }

  async function loginAsUser(payload: LoginPayload): Promise<void> {
    const res = await loginUser(payload);
    persistAuth(res.accessToken, res.refreshToken, res.role);
    profile.value = {
      id: res.id,
      username: payload.username ?? payload.phone ?? "",
      nickname: res.nickname,
      avatar: res.avatar,
      role: res.role,
    };
  }

  async function loginAsAdmin(payload: LoginPayload): Promise<void> {
    const res = await loginAdmin(payload);
    persistAuth(res.accessToken, res.refreshToken, res.role);
    profile.value = {
      id: 0,
      username: payload.username ?? payload.phone ?? "",
      nickname: res.nickname,
      avatar: res.avatar,
      role: res.role,
    };
  }

  async function fetchProfile(): Promise<void> {
    // Dynamically import to avoid circular dependency in stores/api calls
    const { getProfileInfo } = await import("@/api/profile");
    try {
      const res = await getProfileInfo();
      profile.value = {
        id: res.id,
        username: res.username,
        nickname: res.nickname,
        avatar: res.avatar || "",
        role: (res.role || role.value) as UserRole,
      };
    } catch (e) {
      console.warn("Could not fetch user profile", e);
    }
  }

  async function logout(): Promise<void> {
    try {
      // 调用后端登出接口（可能失败，比如token已经过期）
      if (role.value === "ADMIN") {
        await logoutAdmin();
      } else {
        await logoutUser();
      }
    } finally {
      clearAuth();
    }
  }

  return {
    token,
    refreshToken,
    role,
    profile,
    isAuthenticated,
    clearAuth,
    loginAsUser,
    loginAsAdmin,
    fetchProfile,
    logout,
  };
});
