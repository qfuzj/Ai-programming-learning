package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.log.OperationLogQueryDTO;
import com.travel.advisor.entity.OperationLogE;
import com.travel.advisor.mapper.OperationLogMapper;
import com.travel.advisor.service.OperationLogService;
import com.travel.advisor.vo.log.OperationLogListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志
     */
    @Override
    public PageResult<OperationLogListVO> page(OperationLogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperationLogE> wrapper = new LambdaQueryWrapper<OperationLogE>()
                .select(OperationLogE::getId,
                        OperationLogE::getModule,
                        OperationLogE::getAction,
                        OperationLogE::getAdminUsername,
                        OperationLogE::getDescription,
                        OperationLogE::getRequestUrl,
                        OperationLogE::getExecutionTimeMs,
                        OperationLogE::getStatus,
                        OperationLogE::getCreateTime)
                .like(StringUtils.hasText(queryDTO.getModule()), OperationLogE::getModule, queryDTO.getModule())
                .like(StringUtils.hasText(queryDTO.getAction()), OperationLogE::getAction, queryDTO.getAction())
                .like(StringUtils.hasText(queryDTO.getAdminUsername()), OperationLogE::getAdminUsername,
                        queryDTO.getAdminUsername())
                .eq(queryDTO.getStatus() != null, OperationLogE::getStatus, queryDTO.getStatus())
                .orderByDesc(OperationLogE::getCreateTime);

        Page<OperationLogE> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<OperationLogE> pageData = operationLogMapper.selectPage(page, wrapper);
        List<OperationLogListVO> records = pageData.getRecords().stream().map(this::toListVO).toList();

        return PageResult.<OperationLogListVO>builder()
                .records(records)
                .total(pageData.getTotal())
                .pageNum(queryDTO.getPageNum())
                .pageSize(queryDTO.getPageSize())
                .totalPage(pageData.getPages())
                .build();
    }

    private OperationLogListVO toListVO(OperationLogE entity) {
        OperationLogListVO vo = new OperationLogListVO();
        vo.setId(entity.getId());
        vo.setModule(entity.getModule());
        vo.setAction(entity.getAction());
        vo.setAdminUsername(entity.getAdminUsername());
        vo.setDescription(entity.getDescription());
        vo.setRequestUrl(entity.getRequestUrl());
        vo.setExecutionTimeMs(entity.getExecutionTimeMs());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreateTime());
        return vo;
    }

    /**
     * 查询操作日志详情
     */
    @Override
    public OperationLogE detail(Long id) {
        return operationLogMapper.selectById(id);
    }
}
