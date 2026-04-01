package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.dto.comment.CommentAddDTO;
import com.campus.resourcesharing.entity.GoodsComment;
import com.campus.resourcesharing.vo.comment.CommentVO;

import java.util.List;

public interface GoodsCommentService extends IService<GoodsComment> {

	void addComment(String token, CommentAddDTO dto);

	List<CommentVO> listByGoods(Long goodsId);
}
