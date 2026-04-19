export interface ItineraryAiFormModel {
  destination: string;
  days: number;
  startDate?: string;
  endDate?: string;
  budget?: number;
  companionType?: string;
  travelStyle?: string;
  preferredTagsText: string;
}

export function createDefaultItineraryAiForm(): ItineraryAiFormModel {
  return {
    destination: "",
    days: 2,
    startDate: undefined,
    endDate: undefined,
    budget: undefined,
    companionType: undefined,
    travelStyle: undefined,
    preferredTagsText: "",
  };
}
