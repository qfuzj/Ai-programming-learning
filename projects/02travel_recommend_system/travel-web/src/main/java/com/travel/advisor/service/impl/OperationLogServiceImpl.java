package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.log.OperationLogQueryDTO;
import com.travel.advisor.entity.OperationLogE;
import com.travel.advisor.mapper.OperationLogMapper;
import com.travel.advisor.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志
     */
    @Override
    public PageResult<OperationLogE> page(OperationLogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperationLogE> wrapper = new LambdaQueryWrapper<OperationLogE>()
                .like(StringUtils.hasText(queryDTO.getKeyword()), OperationLogE::getDescription, queryDTO.getKeyword())
                .like(StringUtils.hasText(queryDTO.getModule()), OperationLogE::getModule, queryDTO.getModule())
                .like(StringUtils.hasText(queryDTO.getAction()), OperationLogE::getAction, queryDTO.getAction())
                .like(StringUtils.hasText(queryDTO.getAdminUsername()), OperationLogE::getAdminUsername, queryDTO.getAdminUsername())
                .eq(queryDTO.getStatus() != null, OperationLogE::getStatus, queryDTO.getStatus())
                .orderByDesc(OperationLogE::getCreateTime);

        Page<OperationLogE> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<OperationLogE> pageData = operationLogMapper.selectPage(page, wrapper);

        return PageResult.<OperationLogE>builder()
            .records(pageData.getRecords())
            .total(pageData.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
            .totalPage(pageData.getPages())
                .build();
    }

    /**
     * 查询操作日志详情
     */
    @Override
    public OperationLogE detail(Long id) {
        return operationLogMapper.selectById(id);
    }
}
