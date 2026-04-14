package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.region.RegionCreateDTO;
import com.travel.advisor.dto.region.RegionQueryDTO;
import com.travel.advisor.dto.region.RegionUpdateDTO;
import com.travel.advisor.entity.Region;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.service.RegionService;
import com.travel.advisor.utils.BeanCopyUtils;
import com.travel.advisor.vo.region.RegionTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;

    @Override
    public List<RegionTreeVO> getTree() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<Region>()
                .orderByAsc(Region::getLevel)
                .orderByAsc(Region::getSortOrder);
        List<Region> allRegions = regionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(allRegions)) {
            return Collections.emptyList();
        }

        List<RegionTreeVO> allVOList = allRegions.stream()
                .map(region -> {
                    RegionTreeVO vo = new RegionTreeVO();
                    vo.setId(region.getId());
                    vo.setParentId(region.getParentId());
                    vo.setName(region.getName());
                    vo.setShortName(region.getShortName());
                    vo.setLevel(region.getLevel());
                    vo.setCode(region.getCode());
                    vo.setLongitude(region.getLongitude());
                    vo.setLatitude(region.getLatitude());
                    vo.setIsHot(region.getIsHot());
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .toList();

        Map<Long, List<RegionTreeVO>> parentMap = allVOList.stream()
                .collect(Collectors.groupingBy(RegionTreeVO::getParentId));

        for (RegionTreeVO vo : allVOList) {
            vo.setChildren(parentMap.getOrDefault(vo.getId(), Collections.emptyList()));
        }

        return parentMap.getOrDefault(0L, Collections.emptyList());
    }

    @Override
    public PageResult<Region> page(RegionQueryDTO dto, PageQuery pageQuery) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<Region>();
        queryWrapper.orderByAsc(Region::getLevel)
                .orderByAsc(Region::getSortOrder);
        if (dto != null) {
            if (dto.getLevel() != null) {
                queryWrapper.eq(Region::getLevel, dto.getLevel());
            }
            if (StringUtils.hasText(dto.getName())) {
                queryWrapper.like(Region::getName, dto.getName());
            }
            if (StringUtils.hasText(dto.getCode())) {
                queryWrapper.eq(Region::getCode, dto.getCode());
            }
            if (dto.getIsHot() != null) {
                queryWrapper.eq(Region::getIsHot, dto.getIsHot());
            }
            if (dto.getParentId() != null) {
                queryWrapper.eq(Region::getParentId, dto.getParentId());
            }
        }

        Page<Region> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<Region> result = regionMapper.selectPage(page, queryWrapper);

        return PageResult.<Region>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public Long create(RegionCreateDTO dto) {
        Region region = BeanCopyUtils.copy(dto, Region.class);
        regionMapper.insert(region);
        return region.getId();
    }

    @Override
    public void update(Long id, RegionUpdateDTO dto) {
        Region existing = regionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地区不存在");
        }
        Region region = BeanCopyUtils.copy(dto, Region.class);
        region.setId(id);
        regionMapper.updateById(region);
    }

    @Override
    public void delete(Long id) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地区不存在");
        }
        // 检查是否有子节点
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<Region>()
                .eq(Region::getParentId, id);
        Long count = regionMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在子地区，无法删除");
        }
        regionMapper.deleteById(id);
    }
}
