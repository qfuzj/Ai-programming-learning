package com.campus.resourcesharing.dto.favorite;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteAddDTO {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
}
