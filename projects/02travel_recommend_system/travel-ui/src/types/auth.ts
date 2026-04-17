/**
 * 认证与权限相关类型定义。
 */
export type UserRole = "ADMIN" | "USER";
export type LoginType = "username" | "phone";

/**
 * 对应-用户登录 DTO
 */
export interface LoginPayload {
  loginType: LoginType;
  username?: string;
  phone?: string;
  password: string;
  captchaId: string;
  captchaCode: string;
}

/**
 * 对应-登录成功VO
 */
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

/**
 * 对应-验证码VO
 */
export interface CaptchaResponse {
  captchaId: string;
  captchaBase64: string;
}

/**
 * 当前用户信息VO类
 */
export interface CurrentUser {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  role: UserRole;
}

/**
 * 对应-用户注册DTO
 */
export interface RegisterPayload {
  username: string;
  phone: string;
  password: string;
  confirmPassword: string;
  captchaId: string;
  captchaCode: string;
}

/**
 * 对应-重置密码DTO
 */
export interface ResetPasswordPayload {
  phone: string;
  newPassword: string;
  captchaId: string;
  captchaCode: string;
}

// 对应-刷新Token DTO
export interface RefreshTokenPayload {
  refreshToken: string;
}
