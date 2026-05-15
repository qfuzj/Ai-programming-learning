package com.travel.advisor.service.portrait;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortraitSummaryContext {
    private String travelStyle;
    private String budgetLabel;
    private String preferredSeason;
    private List<String> preferredTags;
    private List<String> recentPreferences;
    private String location;
}
