package com.campus.resourcesharing.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminBannerSaveDTO {

    private Long id;

    private String title;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private String linkUrl;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
