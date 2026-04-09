package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.config.SystemConfigQueryDTO;
import com.travel.advisor.dto.config.SystemConfigUpdateDTO;
import com.travel.advisor.entity.SystemConfig;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.SystemConfigMapper;
import com.travel.advisor.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    /**
     * 分页查询系统配置项
     */
    @Override
    public PageResult<SystemConfig> page(SystemConfigQueryDTO queryDTO) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<SystemConfig>()
                .like(StringUtils.hasText(queryDTO.getKeyword()), SystemConfig::getConfigKey, queryDTO.getKeyword())
                .or(StringUtils.hasText(queryDTO.getKeyword()), query -> query.like(SystemConfig::getDescription, queryDTO.getKeyword()))
                .eq(StringUtils.hasText(queryDTO.getConfigGroup()), SystemConfig::getConfigGroup, queryDTO.getConfigGroup())
                .orderByAsc(SystemConfig::getConfigGroup)
                .orderByAsc(SystemConfig::getConfigKey);

        Page<SystemConfig> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<SystemConfig> pageData = systemConfigMapper.selectPage(page, wrapper);

        return PageResult.<SystemConfig>builder()
            .records(pageData.getRecords())
            .total(pageData.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
            .totalPage(pageData.getPages())
                .build();
    }

    @Override
    public SystemConfig detail(String configKey) {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(String configKey, SystemConfigUpdateDTO dto) {
        SystemConfig config = detail(configKey);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统配置不存在");
        }
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setConfigGroup(dto.getConfigGroup());
        config.setDescription(dto.getDescription());
        config.setIsPublic(dto.getIsPublic());
        config.setUpdateTime(LocalDateTime.now());
        systemConfigMapper.updateById(config);
    }
}
