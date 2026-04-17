import type { ItineraryCreatePayload } from "@/api/itinerary";

export type ItineraryDialogType = "create" | "edit";

export type ItineraryFormModel = ItineraryCreatePayload;

export function createDefaultItineraryForm(): ItineraryFormModel {
  return {
    title: "",
    startDate: "",
    endDate: "",
    totalDays: 1,
    status: 1,
    isPublic: 0,
    description: "",
  };
}
