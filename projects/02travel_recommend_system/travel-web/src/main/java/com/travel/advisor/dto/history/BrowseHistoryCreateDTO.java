package com.travel.advisor.dto.history;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户浏览历史记录创建DTO
 */
@Data
public class BrowseHistoryCreateDTO {

    @NotNull(message = "景点ID不能为空")
    private Long scenicId;

    private Integer stayDuration;

    private String source;

    private String deviceType;
}
