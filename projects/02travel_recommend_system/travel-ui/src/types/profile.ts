import type {
  ProfileInfo as ApiProfileInfo,
  ProfilePortraitSummary as ApiProfilePortraitSummary,
  ProfileUpdatePayload,
} from "@/api/profile";

export type ProfileInfo = ApiProfileInfo;
export type ProfilePortraitSummary = ApiProfilePortraitSummary;

export interface TabItem {
  label: string;
  key: string;
}

export interface UpdateProfilePayload extends ProfileUpdatePayload {
  nickname?: string;
  avatar?: string;
  signature?: string;
  gender?: number;
  birthday?: string;
}
