package com.travel.advisor.dto.scenic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量修改景点状态的 DTO，用于接收前端请求中的景点 ID 列表和新的状态值。
 */
@Data
public class ScenicStatusDTO {

    private List<Long> scenicIds;

    @NotNull(message = "状态不能为空")
    private Integer status;
}