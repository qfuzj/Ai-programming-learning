package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.log.OperationLogQueryDTO;
import com.travel.advisor.entity.OperationLogE;
import com.travel.advisor.vo.log.OperationLogListVO;

public interface OperationLogService {

    PageResult<OperationLogListVO> page(OperationLogQueryDTO queryDTO);

    OperationLogE detail(Long id);
}
