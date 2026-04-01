package com.campus.resourcesharing.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSendDTO {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容长度不能超过500")
    private String content;

    private String messageType;
}
