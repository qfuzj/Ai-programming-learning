package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.config.SystemConfigQueryDTO;
import com.travel.advisor.dto.config.SystemConfigUpdateDTO;
import com.travel.advisor.entity.SystemConfig;

public interface SystemConfigService {

    PageResult<SystemConfig> page(SystemConfigQueryDTO queryDTO);

    SystemConfig detail(String configKey);

    void update(String configKey, SystemConfigUpdateDTO dto);
}
