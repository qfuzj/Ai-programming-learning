package com.campus.resourcesharing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.resourcesharing.common.query.PageQuery;
import com.campus.resourcesharing.common.result.PageResult;
import com.campus.resourcesharing.dto.message.MessageSendDTO;
import com.campus.resourcesharing.entity.GoodsMessage;
import com.campus.resourcesharing.vo.message.MessageVO;

public interface GoodsMessageService extends IService<GoodsMessage> {

	void sendMessage(String token, MessageSendDTO dto);

	PageResult<MessageVO> listMessages(String token, PageQuery query);

	void markAsRead(String token, Long id);
}
