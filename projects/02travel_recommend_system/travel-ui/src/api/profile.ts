/**
 * 个人中心模块接口。
 */
import http from "@/api/http";

export interface ProfileInfo {
  id: number;
  username: string;
  nickname: string;
  avatar?: string;
  signature?: string;
  gender?: number;
  birthday?: string;
  role?: string;
}

export interface ProfileUpdatePayload {
  nickname?: string;
  avatar?: string;
  signature?: string;
  gender?: number;
  birthday?: string;
}

export interface ProfilePortraitSummary {
  travelStyle?: string;
  budgetLevel?: string;
  preferredTags?: string[];
  recentBrowsePreferences?: string[];
  summary?: string;
}

export function getProfileInfo(): Promise<ProfileInfo> {
  return http.get("/api/user/profile/me");
}

export function updateProfileInfo(payload: ProfileUpdatePayload): Promise<void> {
  return http.put("/api/user/profile/me", payload);
}

export function getProfilePortrait(): Promise<ProfilePortraitSummary> {
  return http.get("/api/user/profile/portrait");
}

export function updatePreferenceTags(tagIds: number[]): Promise<void> {
  return http.put("/api/user/profile/preference-tags", { tagIds });
}
