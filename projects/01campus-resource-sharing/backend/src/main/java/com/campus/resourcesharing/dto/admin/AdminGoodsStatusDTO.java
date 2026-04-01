package com.campus.resourcesharing.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminGoodsStatusDTO {

    @NotBlank(message = "状态不能为空")
    private String status;
}
