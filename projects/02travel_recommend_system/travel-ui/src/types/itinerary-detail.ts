import type { ItineraryItemPayload } from "@/api/itinerary";

export type ItineraryDayFormModel = ItineraryItemPayload;

export function createDefaultItineraryDayForm(dayNo = 1): ItineraryDayFormModel {
  return {
    dayNo,
    title: "",
    itemType: 1,
    startTime: "",
    endTime: "",
    description: "",
    estimatedCost: 0,
  };
}
