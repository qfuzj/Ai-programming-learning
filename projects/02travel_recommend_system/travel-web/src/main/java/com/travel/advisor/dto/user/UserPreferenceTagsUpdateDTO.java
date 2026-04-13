package com.travel.advisor.dto.user;

import lombok.Data;
import java.util.List;

@Data
public class UserPreferenceTagsUpdateDTO {
    private List<Long> tagIds;
}
