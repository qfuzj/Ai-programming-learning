package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.user.UserQueryDTO;
import com.travel.advisor.vo.user.AdminUserDetailVO;

public interface AdminUserManageService {

    PageResult<AdminUserDetailVO> page(UserQueryDTO queryDTO);

    AdminUserDetailVO detail(Long id);

    void updateStatus(Long id, Integer status);
}