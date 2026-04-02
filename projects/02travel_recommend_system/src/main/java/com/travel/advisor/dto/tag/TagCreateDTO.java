package com.travel.advisor.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagCreateDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotNull(message = "标签类型不能为空")
    private Integer type;

    private String category;

    private String icon;

    private String color;

    private Integer sortOrder;

    private Integer status;
}
