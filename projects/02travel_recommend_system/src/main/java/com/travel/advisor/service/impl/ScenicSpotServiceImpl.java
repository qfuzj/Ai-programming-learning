package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.scenic.*;
import com.travel.advisor.entity.*;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.*;
import com.travel.advisor.service.RegionService;
import com.travel.advisor.service.ScenicSpotService;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.scenic.ScenicDetailVO;
import com.travel.advisor.vo.scenic.ScenicFilterOptionsVO;
import com.travel.advisor.vo.scenic.ScenicImageVO;
import com.travel.advisor.vo.scenic.ScenicListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScenicSpotServiceImpl implements ScenicSpotService {

    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicImageMapper scenicImageMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final TagMapper tagMapper;
    private final RegionMapper regionMapper;
    private final RegionService regionService;

    @Override
    public PageResult<ScenicListVO> page(ScenicQueryDTO dto) {
        ScenicQueryDTO query = dto == null ? new ScenicQueryDTO() : dto;
        LambdaQueryWrapper<ScenicSpot> queryWrapper = buildQueryWrapper(query);

        Page<ScenicSpot> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ScenicSpot> result = scenicSpotMapper.selectPage(page, queryWrapper);

        List<ScenicListVO> records = enrichList(result.getRecords());
        return PageResult.<ScenicListVO>builder()
                .records(records)
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public ScenicDetailVO detail(Long id) {
        ScenicSpot scenicSpot = findById(id);
        ScenicDetailVO vo = buildDetailVO(scenicSpot);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        vo.setIsFavorite(currentUserId != null && isFavorite(currentUserId, scenicSpot.getId()));
        return vo;
    }

    @Override
    public List<ScenicListVO> hotList() {
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getStatus, 1)
                .eq(ScenicSpot::getIsDeleted, 0)
                .orderByDesc(ScenicSpot::getFavoriteCount)
                .orderByDesc(ScenicSpot::getRatingScore)
                .orderByDesc(ScenicSpot::getViewCount)
                .last("limit 10");      // 热门榜单通常只取 Top 10
        return enrichList(scenicSpotMapper.selectList(queryWrapper));
    }

    @Override
    public ScenicFilterOptionsVO filterOptions() {
        ScenicFilterOptionsVO vo = new ScenicFilterOptionsVO();
        vo.setRegions(regionService.getTree());
        vo.setCategories(safeList(scenicSpotMapper.selectCategories()));
        vo.setLevels(safeList(scenicSpotMapper.selectLevels()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long create(ScenicCreateDTO dto) {
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(dto, scenicSpot);
        applyDefaults(scenicSpot);
        scenicSpotMapper.insert(scenicSpot);

        syncTags(scenicSpot.getId(), dto.getTagIds());
        syncImages(scenicSpot.getId(), dto.getImageIds());
        return scenicSpot.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Long id, ScenicUpdateDTO dto) {
        ScenicSpot existing = findById(id);
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(dto, scenicSpot, getNullPropertyNames(dto));
        scenicSpot.setId(id);
        scenicSpot.setCreateTime(existing.getCreateTime());
        scenicSpot.setUpdateTime(LocalDateTime.now());
        scenicSpotMapper.updateById(scenicSpot);

        if (dto.getTagIds() != null) {
            syncTags(id, dto.getTagIds());
        }
        if (dto.getImageIds() != null) {
            syncImages(id, dto.getImageIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        findById(id);
        scenicSpotTagMapper.deleteByScenicSpotId(id);
        scenicImageMapper.delete(new LambdaQueryWrapper<ScenicImage>().eq(ScenicImage::getScenicSpotId, id));
        scenicSpotMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        findById(id);
        ScenicSpot update = new ScenicSpot();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        scenicSpotMapper.updateById(update);
    }

    @Override
    public void batchUpdateStatus(ScenicStatusDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getScenicIds())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "景点ID不能为空");
        }
        for (Long scenicId : dto.getScenicIds()) {
            updateStatus(scenicId, dto.getStatus());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTags(Long scenicSpotId, List<Long> tagIds) {
        findById(scenicSpotId);
        syncTags(scenicSpotId, tagIds);
    }

    @Override
    public List<ScenicImageVO> listImages(Long scenicSpotId) {
        findById(scenicSpotId);
        return scenicImageMapper.selectByScenicSpotId(scenicSpotId).stream()
                .map(this::toImageVO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addImage(Long scenicSpotId, ScenicImageCreateDTO dto) {
        findById(scenicSpotId);
        if (!StringUtils.hasText(dto.getImageUrl()) && dto.getFileResourceId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片URL或文件资源ID至少填写一个");
        }
        ScenicImage scenicImage = new ScenicImage();
        scenicImage.setScenicSpotId(scenicSpotId);
        scenicImage.setFileResourceId(dto.getFileResourceId());
        scenicImage.setImageUrl(StringUtils.hasText(dto.getImageUrl()) ? dto.getImageUrl() : "");
        scenicImage.setImageType(dto.getImageType());
        scenicImage.setTitle(dto.getTitle());
        scenicImage.setSortOrder(dto.getSortOrder());
        scenicImage.setIsCover(dto.getIsCover());
        scenicImageMapper.insert(scenicImage);
        return scenicImage.getId();
    }

    @Override
    public void deleteImage(Long scenicSpotId, Long imageId) {
        findById(scenicSpotId);
        ScenicImage scenicImage = scenicImageMapper.selectOne(new LambdaQueryWrapper<ScenicImage>()
                .eq(ScenicImage::getId, imageId)
                .eq(ScenicImage::getScenicSpotId, scenicSpotId));
        if (scenicImage == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点图片不存在");
        }
        scenicImageMapper.deleteById(imageId);
    }

    /**
     * 构建查询条件 支持根据状态、地区、分类、等级、评分等多维度过滤，并且支持关键词搜索（同时匹配景点名称和地址）
     *
     * @param query 查询参数
     * @return 查询条件构造器
     */
    private LambdaQueryWrapper<ScenicSpot> buildQueryWrapper(ScenicQueryDTO query) {
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(query.getStatus() != null, ScenicSpot::getStatus, query.getStatus())
                .eq(query.getRegionId() != null, ScenicSpot::getRegionId, query.getRegionId())
                .eq(StringUtils.hasText(query.getCategory()), ScenicSpot::getCategory, query.getCategory())
                .eq(StringUtils.hasText(query.getLevel()), ScenicSpot::getLevel, query.getLevel())
                .ge(query.getMinScore() != null, ScenicSpot::getRatingScore, query.getMinScore())
                .eq(ScenicSpot::getIsDeleted, 0);

        // 关键词搜索：同时匹配景点名称和地址
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ScenicSpot::getName, query.getKeyword())
                    .or()
                    .like(ScenicSpot::getAddress, query.getKeyword()));
        }

        applySort(queryWrapper, query.getSortBy(), query.getSortOrder());
        return queryWrapper;
    }

    /**
     * 应用排序规则 支持根据名称、评分、热度（收藏数和浏览数综合）、创建时间等字段进行排序，并且支持升序和降序
     *
     * @param queryWrapper 查询条件构造器
     * @param sortBy       排序字段
     * @param sortOrder    排序顺序（asc或desc）
     */
    private void applySort(LambdaQueryWrapper<ScenicSpot> queryWrapper, String sortBy, String sortOrder) {
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        String orderField = StringUtils.hasText(sortBy) ? sortBy.toLowerCase() : "default";
        switch (orderField) {
            case "name" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getName);

            case "score" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getRatingScore)
                    .orderByDesc(ScenicSpot::getRatingCount);

            case "hot" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getFavoriteCount)
                    .orderBy(true, isAsc, ScenicSpot::getViewCount);

            case "createtime" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getCreateTime);

            default -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getSortOrder)
                    .orderByDesc(ScenicSpot::getCreateTime);
        }
    }

    /**
     * 丰富列表数据 包括地区名称、标签名称、是否收藏等信息，避免N+1查询问题
     *
     * @param scenicSpots 景点列表
     * @return 丰富后的景点列表VO
     */
    private List<ScenicListVO> enrichList(List<ScenicSpot> scenicSpots) {
        if (CollectionUtils.isEmpty(scenicSpots)) {
            return Collections.emptyList();
        }
        Map<Long, String> regionNameMap = loadRegionNameMap(scenicSpots);
        Map<Long, List<String>> tagNameMap = loadTagNameMap(scenicSpots);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return scenicSpots.stream().map(scenicSpot -> {
            ScenicListVO vo = new ScenicListVO();
            vo.setScenicId(scenicSpot.getId());
            vo.setName(scenicSpot.getName());
            vo.setCoverImage(scenicSpot.getCoverImage());
            vo.setRegionName(regionNameMap.get(scenicSpot.getRegionId()));
            vo.setScore(scenicSpot.getRatingScore());
            vo.setLevel(scenicSpot.getLevel());
            vo.setTagList(tagNameMap.getOrDefault(scenicSpot.getId(), Collections.emptyList()));
            vo.setIsFavorite(currentUserId != null && isFavorite(currentUserId, scenicSpot.getId()));
            return vo;
        }).toList();
    }

    private ScenicDetailVO buildDetailVO(ScenicSpot scenicSpot) {
        ScenicDetailVO vo = new ScenicDetailVO();
        vo.setScenicId(scenicSpot.getId());
        vo.setName(scenicSpot.getName());
        vo.setRegionId(scenicSpot.getRegionId());
        vo.setRegionName(loadRegionNameMap(List.of(scenicSpot)).get(scenicSpot.getRegionId()));
        vo.setAddress(scenicSpot.getAddress());
        vo.setLongitude(scenicSpot.getLongitude());
        vo.setLatitude(scenicSpot.getLatitude());
        vo.setCoverImage(scenicSpot.getCoverImage());
        vo.setDescription(scenicSpot.getDescription());
        vo.setDetailContent(scenicSpot.getDetailContent());
        vo.setOpenTime(scenicSpot.getOpenTime());
        vo.setTicketInfo(scenicSpot.getTicketInfo());
        vo.setTicketPrice(scenicSpot.getTicketPrice());
        vo.setLevel(scenicSpot.getLevel());
        vo.setCategory(scenicSpot.getCategory());
        vo.setRatingScore(scenicSpot.getRatingScore());
        vo.setRatingCount(scenicSpot.getRatingCount());
        vo.setViewCount(scenicSpot.getViewCount());
        vo.setFavoriteCount(scenicSpot.getFavoriteCount());
        vo.setBestSeason(scenicSpot.getBestSeason());
        vo.setSuggestedHours(scenicSpot.getSuggestedHours());
        vo.setTips(scenicSpot.getTips());
        vo.setStatus(scenicSpot.getStatus());
        vo.setSortOrder(scenicSpot.getSortOrder());
        vo.setIsRecommended(scenicSpot.getIsRecommended());
        vo.setTagList(loadTagNameMap(List.of(scenicSpot)).getOrDefault(scenicSpot.getId(), Collections.emptyList()));
        vo.setImages(listImages(scenicSpot.getId()));
        return vo;
    }

    /**
     * 加载地区名称映射 避免N+1查询问题
     *
     * @param scenicSpots 景点列表
     * @return 地区ID到地区名称的映射
     */
    private Map<Long, String> loadRegionNameMap(List<ScenicSpot> scenicSpots) {
        List<Long> regionIds = scenicSpots.stream()
                .map(ScenicSpot::getRegionId)   // 取出所有景点的地区ID
                .filter(Objects::nonNull)       // 排除掉没有关联地区的
                .distinct()                     // 去重，比如果多个景点在同一个地区，只查询一次
                .toList();                      // 转成列表，方便后续查询
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(
                        Region::getId,                      // Key:地区ID
                        Region::getName,                    // Value:地区名称
                        (left, right) -> left, // 如果有重复的地区ID（理论上不应该有），保留第一个出现的名称
                        LinkedHashMap::new));               // 使用LinkedHashMap保持查询结果的顺序与输入的regionIds一致
    }

    /**
     * 加载标签名称映射 避免N+1查询问题
     *
     * @param scenicSpots 景点列表
     * @return 景点ID到标签名称列表的映射
     */
    private Map<Long, List<String>> loadTagNameMap(List<ScenicSpot> scenicSpots) {
        // 获取不重复的景点ID列表
        List<Long> scenicIds = scenicSpots.stream().map(ScenicSpot::getId).distinct().toList();
        if (scenicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 根据景点ID列表，查询相关的景点标签关联列表
        List<ScenicSpotTag> scenicSpotTags = scenicSpotTagMapper.selectByScenicSpotIds(scenicIds);
        if (CollectionUtils.isEmpty(scenicSpotTags)) {
            return Collections.emptyMap();
        }
        // 从景点标签关联列表中，过滤出所有不重复的标签ID列表
        List<Long> tagIds = scenicSpotTags.stream().map(ScenicSpotTag::getTagId).distinct().toList();
        // 根据标签ID列表批量查询标签信息，并映射为<标签ID,标签名称>Map，如果有相同的标签ID，取第一个，并保持查询结果的顺序
        Map<Long, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<String>> result = new HashMap<>();
        for (ScenicSpotTag scenicSpotTag : scenicSpotTags) {
            String tagName = tagNameMap.get(scenicSpotTag.getTagId());
            if (tagName == null) {
                continue;
            }
            // 映射为 <景点ID，景点ID下的标签名称列表>Map
            result.computeIfAbsent(scenicSpotTag.getScenicSpotId(), key -> new ArrayList<>()).add(tagName);
        }
        return result;
    }

    /**
     * @param userId
     * @param scenicSpotId
     * @return
     */
    private boolean isFavorite(Long userId, Long scenicSpotId) {
        Long count = scenicSpotMapper.countUserFavorite(userId, scenicSpotId);
        return count != null && count > 0;
    }

    private ScenicSpot findById(Long id) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(id);
        if (scenicSpot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "景点不存在");
        }
        return scenicSpot;
    }

    private void applyDefaults(ScenicSpot scenicSpot) {
        if (scenicSpot.getStatus() == null) {
            scenicSpot.setStatus(1);
        }
        if (scenicSpot.getSortOrder() == null) {
            scenicSpot.setSortOrder(0);
        }
        if (scenicSpot.getIsRecommended() == null) {
            scenicSpot.setIsRecommended(0);
        }
        if (scenicSpot.getRatingScore() == null) {
            scenicSpot.setRatingScore(BigDecimal.ZERO);
        }
        if (scenicSpot.getRatingCount() == null) {
            scenicSpot.setRatingCount(0);
        }
        if (scenicSpot.getViewCount() == null) {
            scenicSpot.setViewCount(0);
        }
        if (scenicSpot.getFavoriteCount() == null) {
            scenicSpot.setFavoriteCount(0);
        }
    }

    private void syncTags(Long scenicSpotId, List<Long> tagIds) {
        scenicSpotTagMapper.deleteByScenicSpotId(scenicSpotId);
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }

        List<ScenicSpotTag> scenicSpotTags = tagIds.stream().map(tagId -> {
            ScenicSpotTag scenicSpotTag = new ScenicSpotTag();
            scenicSpotTag.setScenicSpotId(scenicSpotId);
            scenicSpotTag.setTagId(tagId);
            scenicSpotTag.setCreateTime(LocalDateTime.now());
            return scenicSpotTag;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(scenicSpotTags)) {
            scenicSpotTagMapper.insertBatch(scenicSpotTags);
        }
    }

    private void syncImages(Long scenicSpotId, List<Long> imageIds) {
        scenicImageMapper.delete(new LambdaQueryWrapper<ScenicImage>().eq(ScenicImage::getScenicSpotId, scenicSpotId));
        if (CollectionUtils.isEmpty(imageIds)) {
            return;
        }
        final int[] index = {0};
        List<ScenicImage> scenicImages = imageIds.stream().map(imageId -> {
            ScenicImage scenicImage = new ScenicImage();
            scenicImage.setScenicSpotId(scenicSpotId);
            scenicImage.setFileResourceId(imageId);
            scenicImage.setImageUrl("");
            scenicImage.setImageType(1);
            scenicImage.setTitle(null);
            scenicImage.setSortOrder(index[0]);
            scenicImage.setIsCover(index[0] == 0 ? 1 : 0);
            index[0]++;
            return scenicImage;
        }).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(scenicImages)) {
            scenicImageMapper.insertBatch(scenicImages);
        }
    }

    private ScenicImageVO toImageVO(ScenicImage scenicImage) {
        ScenicImageVO vo = new ScenicImageVO();
        vo.setId(scenicImage.getId());
        vo.setFileResourceId(scenicImage.getFileResourceId());
        vo.setImageUrl(scenicImage.getImageUrl());
        vo.setImageType(scenicImage.getImageType());
        vo.setTitle(scenicImage.getTitle());
        vo.setSortOrder(scenicImage.getSortOrder());
        vo.setIsCover(scenicImage.getIsCover());
        return vo;
    }

    private List<String> safeList(List<String> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.emptyList();
        }
        return source;
    }

    /**
     * 获取对象中值为null的属性名称列表，供BeanUtils.copyProperties时忽略这些属性，避免覆盖原有值
     * @param source 源对象
     * @return 值为null的属性名称数组
     */
    private String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = Arrays.stream(pds)
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .collect(Collectors.toSet());
        return emptyNames.toArray(new String[0]);
    }
}