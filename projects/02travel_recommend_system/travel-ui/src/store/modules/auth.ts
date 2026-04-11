import { computed } from "vue";
import { defineStore } from "pinia";
import type { LoginPayload } from "@/types/auth";
import { useUserStore } from "@/store/modules/user";

export const useAuthStore = defineStore("auth", () => {
  const userStore = useUserStore();

  const token = computed(() => userStore.token);
  const refreshToken = computed(() => userStore.refreshToken);
  const role = computed(() => userStore.role);
  const profile = computed(() => userStore.profile);
  const isAuthenticated = computed(() => userStore.isAuthenticated);

  function clearAuth(): void {
    userStore.clearAuth();
  }

  async function loginUser(payload: LoginPayload): Promise<void> {
    await userStore.loginAsUser(payload);
  }

  async function loginAdmin(payload: LoginPayload): Promise<void> {
    await userStore.loginAsAdmin(payload);
  }

  async function logout(): Promise<void> {
    await userStore.logout();
  }

  return {
    token,
    refreshToken,
    role,
    profile,
    isAuthenticated,
    clearAuth,
    loginUser,
    loginAdmin,
    logout,
  };
});
