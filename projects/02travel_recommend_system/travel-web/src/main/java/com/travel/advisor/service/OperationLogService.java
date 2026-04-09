package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.log.OperationLogQueryDTO;
import com.travel.advisor.entity.OperationLogE;

public interface OperationLogService {

    PageResult<OperationLogE> page(OperationLogQueryDTO queryDTO);

    OperationLogE detail(Long id);
}
