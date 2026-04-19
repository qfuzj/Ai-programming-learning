import type { ItineraryItemPayload } from "@/api/itinerary";

export interface ItineraryDayFormModel extends ItineraryItemPayload {
  id?: number;
}

export type ItineraryItemDialogMode = "create" | "edit";

export function createDefaultItineraryDayForm(dayNo = 1): ItineraryDayFormModel {
  return {
    id: undefined,
    dayNo,
    title: "",
    itemType: 1,
    startTime: "",
    endTime: "",
    description: "",
    location: "",
    estimatedCost: 0,
    notes: "",
    sortOrder: undefined,
    scenicSpotId: undefined,
    longitude: undefined,
    latitude: undefined,
  };
}
