/**
 * 认证接口：登录、登出。
 */
import type {
  CaptchaResponse,
  LoginPayload,
  LoginResponse,
  RefreshTokenPayload,
  RegisterPayload,
  ResetPasswordPayload,
  UserRole,
} from "@/types/auth";
import http from "@/api/http";

interface LoginRawResponse {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
  roleCode?: string;
  userInfo?: {
    userId: number;
    username?: string;
    avatar?: string;
    phone?: string;
  };
  adminInfo?: {
    adminId: number;
    username?: string;
    realName?: string;
    avatar?: string;
  };
}

function normalizeLoginResponse(
  raw: LoginRawResponse,
  defaultRole: "USER" | "ADMIN"
): LoginResponse {
  // 后端 roleCode 当前是“角色编码”（如 super_admin），而不是角色类型。
  // 前端仅区分 ADMIN/USER，直接按登录入口约定使用 defaultRole。
  const role: UserRole = defaultRole;
  const nickname =
    raw.adminInfo?.realName ?? raw.adminInfo?.username ?? raw.userInfo?.username ?? "未知用户";

  const id = raw.adminInfo?.adminId ?? raw.userInfo?.userId ?? 0;
  const avatar = raw.adminInfo?.avatar ?? raw.userInfo?.avatar ?? "";

  return {
    accessToken: raw.accessToken,
    refreshToken: raw.refreshToken,
    tokenType: raw.tokenType,
    expiresIn: raw.expiresIn,
    role,
    nickname,
    id,
    avatar,
  };
}

export function getCaptcha(
  bizType: "login" | "register" | "reset_password" = "login"
): Promise<CaptchaResponse> {
  return http.get("/api/common/auth/captcha", { params: { bizType } });
}

export function loginUser(payload: LoginPayload): Promise<LoginResponse> {
  return http
    .post<any, LoginRawResponse>("/api/user/auth/login", payload)
    .then((res) => normalizeLoginResponse(res, "USER"));
}

export function loginAdmin(payload: LoginPayload): Promise<LoginResponse> {
  return http
    .post<any, LoginRawResponse>("/api/admin/auth/login", payload)
    .then((res) => normalizeLoginResponse(res, "ADMIN"));
}

export function registerUser(
  payload: RegisterPayload
): Promise<{ userId: number; username: string }> {
  return http.post("/api/user/auth/register", payload);
}

export function resetPassword(payload: ResetPasswordPayload): Promise<void> {
  return http.post("/api/user/auth/reset-password", payload);
}

export function refreshUserToken(payload: RefreshTokenPayload): Promise<LoginResponse> {
  return http
    .post<any, LoginRawResponse>("/api/user/auth/refresh-token", payload)
    .then((res) => normalizeLoginResponse(res, "USER"));
}

export function refreshAdminToken(payload: RefreshTokenPayload): Promise<LoginResponse> {
  return http
    .post<any, LoginRawResponse>("/api/admin/auth/refresh-token", payload)
    .then((res) => normalizeLoginResponse(res, "ADMIN"));
}

export function logoutUser(): Promise<void> {
  return http.post("/api/user/auth/logout");
}

export function logoutAdmin(): Promise<void> {
  return http.post("/api/admin/auth/logout");
}
