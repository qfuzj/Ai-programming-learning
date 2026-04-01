package com.campus.resourcesharing.dto.goods;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsUpdateDTO extends GoodsAddDTO {

    @NotNull(message = "商品ID不能为空")
    private Long id;
}
