package com.campus.resourcesharing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.common.result.Result;
import com.campus.resourcesharing.entity.GoodsComment;
import com.campus.resourcesharing.query.admin.AdminCommentPageQuery;
import com.campus.resourcesharing.service.admin.AdminUserLookupService;
import com.campus.resourcesharing.service.GoodsCommentService;
import com.campus.resourcesharing.utils.JwtUtil;
import com.campus.resourcesharing.vo.admin.AdminCommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController extends AdminBaseController {

    private final GoodsCommentService goodsCommentService;
    private final AdminUserLookupService adminUserLookupService;

    public AdminCommentController(JwtUtil jwtUtil,
                                  GoodsCommentService goodsCommentService,
                                  AdminUserLookupService adminUserLookupService) {
        super(jwtUtil);
        this.goodsCommentService = goodsCommentService;
        this.adminUserLookupService = adminUserLookupService;
    }

    @GetMapping("/page")
    public Result<PageResult<AdminCommentVO>> page(@RequestHeader("Authorization") String authorization,
                                                   @Valid AdminCommentPageQuery query) {
        assertAdmin(authorization);

        boolean hasFromKeyword = query.getFromUserKeyword() != null && !query.getFromUserKeyword().isBlank();
        boolean hasToKeyword = query.getToUserKeyword() != null && !query.getToUserKeyword().isBlank();
        Set<Long> fromUserIds = adminUserLookupService.findUserIdsByKeyword(query.getFromUserKeyword());
        Set<Long> toUserIds = adminUserLookupService.findUserIdsByKeyword(query.getToUserKeyword());

        if ((hasFromKeyword && fromUserIds.isEmpty()) || (hasToKeyword && toUserIds.isEmpty())) {
            return Result.success(PageResult.empty(query.getPageNum(), query.getPageSize()));
        }

        LambdaQueryWrapper<GoodsComment> wrapper = new LambdaQueryWrapper<GoodsComment>()
                .eq(query.getGoodsId() != null, GoodsComment::getGoodsId, query.getGoodsId())
                .eq(query.getFromUserId() != null, GoodsComment::getFromUserId, query.getFromUserId())
                .eq(query.getToUserId() != null, GoodsComment::getToUserId, query.getToUserId())
                .in(hasFromKeyword, GoodsComment::getFromUserId, fromUserIds)
                .in(hasToKeyword, GoodsComment::getToUserId, toUserIds)
                .eq(query.getScore() != null, GoodsComment::getScore, query.getScore())
                .orderByDesc(GoodsComment::getCreateTime);

        Page<GoodsComment> page = goodsCommentService.page(query.toPage(), wrapper);

        Set<Long> userIds = page.getRecords().stream()
                .flatMap(item -> List.of(item.getFromUserId(), item.getToUserId()).stream())
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = adminUserLookupService.buildUserDisplayNameMap(userIds);

        List<AdminCommentVO> records = page.getRecords().stream().map(item -> {
            AdminCommentVO vo = new AdminCommentVO();
            vo.setId(item.getId());
            vo.setOrderId(item.getOrderId());
            vo.setGoodsId(item.getGoodsId());
            vo.setFromUserId(item.getFromUserId());
            vo.setFromUserName(userNameMap.get(item.getFromUserId()));
            vo.setToUserId(item.getToUserId());
            vo.setToUserName(userNameMap.get(item.getToUserId()));
            vo.setScore(item.getScore());
            vo.setContent(item.getContent());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).toList();

        return Result.success(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        assertAdmin(authorization);
        goodsCommentService.removeById(id);
        return Result.success("删除成功", null);
    }
}
