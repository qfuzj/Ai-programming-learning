package com.campus.resourcesharing.dto.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentAddDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "被评价用户ID不能为空")
    private Long toUserId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    @Size(max = 500, message = "评价内容长度不能超过500")
    private String content;
}
