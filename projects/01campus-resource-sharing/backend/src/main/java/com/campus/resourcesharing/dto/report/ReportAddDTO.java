package com.campus.resourcesharing.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportAddDTO {

    @NotBlank(message = "举报对象类型不能为空")
    @Pattern(regexp = "^(goods|user)$", message = "举报对象类型仅支持 goods 或 user")
    private String targetType;

    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 255, message = "举报原因长度不能超过255")
    private String reason;

    @Size(max = 500, message = "举报描述长度不能超过500")
    private String description;
}
