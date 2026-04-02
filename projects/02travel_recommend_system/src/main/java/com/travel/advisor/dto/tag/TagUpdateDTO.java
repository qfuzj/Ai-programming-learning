package com.travel.advisor.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagUpdateDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String category;

    private String icon;

    private String color;

    private Integer sortOrder;

    private Integer status;
}
