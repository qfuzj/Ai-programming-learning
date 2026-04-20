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
import com.travel.advisor.utils.JsonUtils;
import com.travel.advisor.utils.RedisUtils;
import com.travel.advisor.vo.region.RegionTreeVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;
    private final RedisUtils redisUtils;

    private static final String REGION_TREE_CACHE_KEY = "region:tree";
    private static final Duration REGION_TREE_CACHE_TTL = Duration.ofHours(24);

    @Override
    public List<RegionTreeVO> getTree() {
        String cached = redisUtils.get(REGION_TREE_CACHE_KEY);
        if (cached != null && !cached.isBlank()) {
            return JsonUtils.fromJson(cached, new TypeReference<List<RegionTreeVO>>() {
            });
        }

        List<RegionTreeVO> tree = buildTreeFromDb();
        redisUtils.set(REGION_TREE_CACHE_KEY, JsonUtils.toJson(tree), REGION_TREE_CACHE_TTL);
        return tree;
    }

    /**
     * 私有方法，基于数据库构建区域树结构并返回顶级节点列表
     * <p>
     * 将 Region 按 level、sortOrder 正序排序查询
     * 将查询出的 Region 转换为 RegionTreeVO，并建立父子关系
     * 返回顶层（parentId 为 0）的 RegionTreeVO 列表
     */
    private List<RegionTreeVO> buildTreeFromDb() {
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

        // 注意：parentMap 中的列表与 allVOList 共享同一批对象引用，直接修改 vo.setChildren() 会同步反映到 parentMap 中
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
        evictTreeCache();
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
        evictTreeCache();
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
        evictTreeCache();
    }

    private void evictTreeCache() {
        redisUtils.delete(REGION_TREE_CACHE_KEY);
    }
}
