package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.message.MessageSendDTO;
import com.campus.resourcesharing.service.GoodsMessageService;
import com.campus.resourcesharing.vo.message.MessageVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final GoodsMessageService goodsMessageService;

    public MessageController(GoodsMessageService goodsMessageService) {
        this.goodsMessageService = goodsMessageService;
    }

    @PostMapping("/send")
    public Result<Void> send(@RequestHeader("Authorization") String authorization,
                             @Valid @RequestBody MessageSendDTO dto) {
        goodsMessageService.sendMessage(extractToken(authorization), dto);
        return Result.success("发送成功", null);
    }

    @GetMapping("/list")
    public Result<PageResult<MessageVO>> list(@RequestHeader("Authorization") String authorization,
                                               @Valid PageQuery query) {
        return Result.success(goodsMessageService.listMessages(extractToken(authorization), query));
    }

    @PutMapping("/read/{id}")
    public Result<Void> read(@RequestHeader("Authorization") String authorization,
                             @PathVariable Long id) {
        goodsMessageService.markAsRead(extractToken(authorization), id);
        return Result.success("已标记已读", null);
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
