package com.campus.resourcesharing.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminReportReviewDTO {

    @NotBlank(message = "审核结果不能为空")
    private String status;

    private String handleResult;
}
