export interface ItineraryAiFormModel {
  destination: string;
  days: number;
  budget?: number;
  companionType?: string;
  travelStyle?: string;
  preferredTagsText: string;
}

export function createDefaultItineraryAiForm(): ItineraryAiFormModel {
  return {
    destination: "",
    days: 2,
    budget: undefined,
    companionType: undefined,
    travelStyle: undefined,
    preferredTagsText: "",
  };
}
