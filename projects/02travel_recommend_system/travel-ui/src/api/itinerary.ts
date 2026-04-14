/**
 * 行程模块接口。
 */
import http from "@/api/http";
import type { PageQuery, PageResult } from "@/types/api";

export interface ItineraryItem {
  id: number;
  title: string;
  coverImage?: string;
  startDate?: string;
  endDate?: string;
  totalDays?: number;
  destinationRegionId?: number;
  description?: string;
  estimatedBudget?: number;
  travelCompanion?: string;
  isPublic?: number;
  source?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
  days?: ItineraryDayItem[];
}

export interface ItineraryQuery extends PageQuery {
  status?: number;
  keyword?: string;
  isPublic?: number;
}

export interface ItineraryCreatePayload {
  title: string;
  coverImage?: string;
  startDate?: string;
  endDate?: string;
  totalDays: number;
  destinationRegionId?: number;
  description?: string;
  estimatedBudget?: number;
  travelCompanion?: string;
  isPublic?: number;
  status?: number;
}

export interface ItineraryItemPayload {
  dayNo: number;
  scenicSpotId?: number;
  sortOrder?: number;
  itemType?: number;
  title: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  location?: string;
  longitude?: number;
  latitude?: number;
  estimatedCost?: number;
  notes?: string;
}

export interface ItineraryDayItem {
  dayNo: number;
  items: Array<{
    id: number;
    scenicSpotId?: number;
    dayNo: number;
    sortOrder?: number;
    itemType?: number;
    title: string;
    description?: string;
    startTime?: string;
    endTime?: string;
    location?: string;
    longitude?: number;
    latitude?: number;
    estimatedCost?: number;
    notes?: string;
    createTime?: string;
  }>;
}

export function getItineraryPage(query: ItineraryQuery): Promise<PageResult<ItineraryItem>> {
  return http.get("/api/user/travel-plans", { params: query });
}

export function getItineraryDetail(id: number): Promise<ItineraryItem> {
  return http.get(`/api/user/travel-plans/${id}`);
}

export function createItinerary(payload: ItineraryCreatePayload): Promise<number> {
  return http.post("/api/user/travel-plans", payload);
}

export function updateItinerary(id: number, payload: ItineraryCreatePayload): Promise<void> {
  return http.put(`/api/user/travel-plans/${id}`, payload);
}

export function deleteItinerary(id: number): Promise<void> {
  return http.delete(`/api/user/travel-plans/${id}`);
}

export function addItineraryItem(planId: number, payload: ItineraryItemPayload): Promise<number> {
  return http.post(`/api/user/travel-plans/${planId}/items`, payload);
}

export function deleteItineraryItem(planId: number, itemId: number): Promise<void> {
  return http.delete(`/api/user/travel-plans/${planId}/items/${itemId}`);
}

export function generateItineraryByAi(payload: {
  destination: string;
  days: number;
  budget?: number;
  companionType?: string;
  travelStyle?: string;
  preferredTags?: string[];
}): Promise<unknown> {
  void payload;
  return Promise.reject(new Error("后端暂未提供 /api/user/travel-plans/ai-generate 接口"));
}
