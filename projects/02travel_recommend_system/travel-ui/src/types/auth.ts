/**
 * 认证与权限相关类型定义。
 */
export type UserRole = "ADMIN" | "USER";
export type LoginType = "username" | "phone";

export interface LoginPayload {
  username?: string;
  phone?: string;
  password: string;
  loginType: LoginType;
  captchaId: string;
  captchaCode: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
  role: UserRole;
  nickname: string;
  id: number;
  avatar: string;
}

export interface CaptchaResponse {
  captchaId: string;
  captchaBase64: string;
}

export interface CurrentUser {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  role: UserRole;
}

export interface RegisterPayload {
  username: string;
  phone: string;
  password: string;
  confirmPassword: string;
  captchaId: string;
  captchaCode: string;
}

export interface ResetPasswordPayload {
  phone: string;
  newPassword: string;
  captchaId: string;
  captchaCode: string;
}

export interface RefreshTokenPayload {
  refreshToken: string;
}
