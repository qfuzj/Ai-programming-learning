package com.campus.resourcesharing.dto.goods;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsAddDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.00", message = "售价不能小于0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "原价不能小于0")
    private BigDecimal originalPrice;

    @NotBlank(message = "成色不能为空")
    private String conditionLevel;

    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;

    private String tradeLocation;

    @NotBlank(message = "主图不能为空")
    private String mainImage;

    private List<String> imageList;
}
