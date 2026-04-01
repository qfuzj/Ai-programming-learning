package com.campus.resourcesharing.controller.user;

import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.dto.comment.CommentAddDTO;
import com.campus.resourcesharing.service.GoodsCommentService;
import com.campus.resourcesharing.vo.comment.CommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final GoodsCommentService goodsCommentService;

    public CommentController(GoodsCommentService goodsCommentService) {
        this.goodsCommentService = goodsCommentService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody CommentAddDTO dto) {
        goodsCommentService.addComment(extractToken(authorization), dto);
        return Result.success("评价成功", null);
    }

    @GetMapping("/listByGoods/{goodsId}")
    public Result<List<CommentVO>> listByGoods(@PathVariable Long goodsId) {
        return Result.success(goodsCommentService.listByGoods(goodsId));
    }

    private String extractToken(String authorization) {
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
