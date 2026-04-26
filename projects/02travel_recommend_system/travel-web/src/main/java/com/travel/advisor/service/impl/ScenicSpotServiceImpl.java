package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.enums.FileResourceStatus;
import com.travel.advisor.common.enums.ScenicLevel;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.scenic.*;
import com.travel.advisor.entity.*;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.*;
import com.travel.advisor.common.enums.BizType;
import com.travel.advisor.service.FileService;
import com.travel.advisor.service.RegionService;
import com.travel.advisor.service.ScenicSpotService;
import com.travel.advisor.utils.FileResourceIds;
import com.travel.advisor.utils.SecurityUtils;
import com.travel.advisor.vo.region.RegionTreeVO;
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

    private static final int REVIEW_STATUS_APPROVED = 1;

    private final ScenicSpotMapper scenicSpotMapper;
    private final ScenicImageMapper scenicImageMapper;
    private final ScenicSpotTagMapper scenicSpotTagMapper;
    private final TagMapper tagMapper;
    private final RegionMapper regionMapper;
    private final RegionService regionService;
    private final UserReviewMapper userReviewMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final FileResourceMapper fileResourceMapper;
    private final FileService fileService;

    @Override
    public List<ScenicSpot> listByIdsWithStatus(Collection<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return scenicSpotMapper.selectList(
                new LambdaQueryWrapper<ScenicSpot>()
                        .in(ScenicSpot::getId, ids)
                        .eq(status != null, ScenicSpot::getStatus, status)
        );
    }

    /**
     * 分页查询景点列表。
     */
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

    /**
     * 查询景点详情，并在返回后递增浏览量。
     */
    @Override
    public ScenicDetailVO detail(Long id) {
        ScenicSpot scenicSpot = findById(id);
        ScenicDetailVO vo = buildDetailVO(scenicSpot);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        vo.setIsFavorite(currentUserId != null && isFavorite(currentUserId, scenicSpot.getId()));
        
        LambdaUpdateWrapper<ScenicSpot> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScenicSpot::getId, id)
                .setSql("view_count = view_count + 1");
        scenicSpotMapper.update(null, updateWrapper);
        
        return vo;
    }

    @Override
    public ScenicDetailVO adminDetail(Long id) {
        ScenicSpot scenicSpot = findById(id);
        return buildDetailVO(scenicSpot);
    }

    /**
     * 按收藏数、评分、浏览量排序，返回热门景点前 10 条。
     */
    @Override
    public List<ScenicListVO> hotList() {
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(ScenicSpot::getStatus, 1)
                .eq(ScenicSpot::getIsDeleted, 0)
                .orderByDesc(ScenicSpot::getFavoriteCount)
                .orderByDesc(ScenicSpot::getScore)
                .orderByDesc(ScenicSpot::getViewCount)
                .last("limit 10");
        return enrichList(scenicSpotMapper.selectList(queryWrapper));
    }

    /**
     * 查询景点筛选项。
     */
    @Override
    public ScenicFilterOptionsVO filterOptions() {
        ScenicFilterOptionsVO vo = new ScenicFilterOptionsVO();
        vo.setRegions(regionService.getTree());
        vo.setCategories(safeList(scenicSpotMapper.selectCategories()));
        vo.setLevels(Arrays.stream(ScenicLevel.values())
                .filter(level -> level != ScenicLevel.NONE)
                .map(ScenicLevel::getCode)
                .toList());
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
        bindCoverImage(dto.getCoverImage(), scenicSpot.getId());
        return scenicSpot.getId();
    }

    /**
     * 按非空字段更新景点信息。
     */
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
        bindCoverImage(dto.getCoverImage(), id);
    }

    /**
     * 删除景点及其关联的标签、图片记录。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        findById(id);
        scenicSpotTagMapper.deleteByScenicSpotId(id);
        scenicImageMapper.delete(new LambdaQueryWrapper<ScenicImage>().eq(ScenicImage::getScenicSpotId, id));
        scenicSpotMapper.deleteById(id);
    }

    /**
     * 更新单个景点状态。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        findById(id);
        ScenicSpot update = new ScenicSpot();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        scenicSpotMapper.updateById(update);
    }

    /**
     * 批量更新景点状态。
     * 使用单条 UPDATE ... WHERE id IN (...) 保证同批 update_time 严格一致，
     * 并在事务内先校验所有 ID 均存在，避免部分生效。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateStatus(ScenicStatusDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getScenicIds())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "景点ID不能为空");
        }
        List<Long> ids = dto.getScenicIds();
        Long existing = scenicSpotMapper.selectCount(
                new LambdaQueryWrapper<ScenicSpot>().in(ScenicSpot::getId, ids));
        if (existing == null || existing.intValue() != ids.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部分景点不存在");
        }
        LambdaUpdateWrapper<ScenicSpot> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ScenicSpot::getId, ids)
                .set(ScenicSpot::getStatus, dto.getStatus())
                .set(ScenicSpot::getUpdateTime, LocalDateTime.now());
        scenicSpotMapper.update(null, wrapper);
    }

    /**
     * 覆盖景点标签关联。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTags(Long scenicSpotId, List<Long> tagIds) {
        findById(scenicSpotId);
        syncTags(scenicSpotId, tagIds);
    }

    @Override
    public List<ScenicImageVO> listImages(Long scenicSpotId) {
        findById(scenicSpotId);
        List<ScenicImage> images = scenicImageMapper.selectByScenicSpotId(scenicSpotId);
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }
        // 批量解析 fileResourceId → URL，避免在 toImageVO 内逐条 selectById 形成 N+1
        List<Long> fileIds = images.stream()
                .map(ScenicImage::getFileResourceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> urlMap = fileIds.isEmpty()
                ? Collections.emptyMap()
                : fileResourceMapper.selectBatchIds(fileIds).stream()
                .filter(fr -> fr.getUrl() != null)
                .collect(Collectors.toMap(FileResource::getId, FileResource::getUrl, (a, b) -> a));
        return images.stream().map(img -> toImageVO(img, urlMap)).toList();
    }

    /**
     * 新增景点图片记录，支持通过 URL 或文件资源 ID 关联图片。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addImage(Long scenicSpotId, ScenicImageCreateDTO dto) {
        findById(scenicSpotId);
        if (!StringUtils.hasText(dto.getImageUrl()) && dto.getFileResourceId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片URL或文件资源ID至少填写一个");
        }
        if (dto.getFileResourceId() != null) {
            FileResource fileResource = fileResourceMapper.selectById(dto.getFileResourceId());
            // 反转 equals 顺序：status 字段可能为 null，避免 NPE
            if (fileResource == null
                    || FileResourceStatus.DELETED.getCode().equals(fileResource.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件资源不存在或已删除");
            }
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
        if (dto.getFileResourceId() != null) {
            fileService.bindFilesToBiz(List.of(dto.getFileResourceId()), scenicSpotId, BizType.SCENIC);
        }
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

    private void collectAllIds(RegionTreeVO node, Set<Long> ids) {
        if (node == null) return;
        ids.add(node.getId());
        if (node.getChildren() != null) {
            for (RegionTreeVO child : node.getChildren()) {
                collectAllIds(child, ids);
            }
        }
    }

    private RegionTreeVO findNode(List<RegionTreeVO> tree, Long targetId) {
        if (tree == null) return null;
        for (RegionTreeVO node : tree) {
            if (node.getId().equals(targetId)) {
                return node;
            }
            RegionTreeVO found = findNode(node.getChildren(), targetId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 返回指定地区及其子地区 ID 集合。
     */
    private Set<Long> getAllDescendantRegionIds(Long regionId) {
        Set<Long> ids = new HashSet<>();
        if (regionId == null) return ids;

        List<RegionTreeVO> tree = regionService.getTree();
        RegionTreeVO targetNode = findNode(tree, regionId);

        if (targetNode != null) {
            collectAllIds(targetNode, ids);
        } else {
            ids.add(regionId);
        }
        return ids;
    }

    /**
     * 构建景点分页查询条件。
     */
    private LambdaQueryWrapper<ScenicSpot> buildQueryWrapper(ScenicQueryDTO query) {
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<ScenicSpot>()
                .eq(query.getStatus() != null, ScenicSpot::getStatus, query.getStatus());

        if (query.getRegionId() != null) {
            queryWrapper.in(ScenicSpot::getRegionId, getAllDescendantRegionIds(query.getRegionId()));
        }

        queryWrapper.eq(StringUtils.hasText(query.getCategory()), ScenicSpot::getCategory, query.getCategory())
                .eq(StringUtils.hasText(query.getLevel()), ScenicSpot::getLevel, query.getLevel())
                .eq(ScenicSpot::getIsDeleted, 0);

        List<Long> selectedTagIds = new ArrayList<>();
        if (query.getTagId() != null) {
            selectedTagIds.add(query.getTagId());
        }
        if (!CollectionUtils.isEmpty(query.getTagIds())) {
            selectedTagIds.addAll(query.getTagIds());
        }
        selectedTagIds = selectedTagIds.stream().filter(Objects::nonNull).distinct().toList();

        if (!selectedTagIds.isEmpty()) {
            List<Long> spotIds = scenicSpotTagMapper.selectList(
                            new LambdaQueryWrapper<ScenicSpotTag>()
                                    .in(ScenicSpotTag::getTagId, selectedTagIds))
                    .stream().map(ScenicSpotTag::getScenicSpotId).distinct().toList();
            if (spotIds.isEmpty()) {
                queryWrapper.eq(ScenicSpot::getId, -1L);
            } else {
                queryWrapper.in(ScenicSpot::getId, spotIds);
            }
        }

        // 标签作用域+分类筛选：先查符合条件的标签，再查关联的景点
        if (StringUtils.hasText(query.getTagScope()) || StringUtils.hasText(query.getTagCategory())) {
            LambdaQueryWrapper<Tag> tagQuery = new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getStatus, 1);
            if (StringUtils.hasText(query.getTagScope())) {
                tagQuery.and(w -> w.eq(Tag::getScope, query.getTagScope()).or().eq(Tag::getScope, "BOTH"));
            }
            if (StringUtils.hasText(query.getTagCategory())) {
                tagQuery.eq(Tag::getCategory, query.getTagCategory());
            }
            List<Long> tagIds = tagMapper.selectList(tagQuery).stream()
                    .map(Tag::getId)
                    .toList();
            if (tagIds.isEmpty()) {
                queryWrapper.eq(ScenicSpot::getId, -1L);
            } else {
                List<Long> spotIds = scenicSpotTagMapper.selectList(
                                new LambdaQueryWrapper<ScenicSpotTag>()
                                        .in(ScenicSpotTag::getTagId, tagIds))
                        .stream().map(ScenicSpotTag::getScenicSpotId).distinct().toList();
                if (spotIds.isEmpty()) {
                    queryWrapper.eq(ScenicSpot::getId, -1L);
                } else {
                    queryWrapper.in(ScenicSpot::getId, spotIds);
                }
            }
        }

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
     * 应用排序规则，兼容前端的 `createdAt` 和实体字段 `createTime`。
     */
    private void applySort(LambdaQueryWrapper<ScenicSpot> queryWrapper, String sortBy, String sortOrder) {
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        String orderField = StringUtils.hasText(sortBy) ? sortBy.toLowerCase() : "default";
        switch (orderField) {
            case "name" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getName);

            case "score" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getScore)
                    .orderByDesc(ScenicSpot::getRatingCount);

            case "hot" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getFavoriteCount)
                    .orderBy(true, isAsc, ScenicSpot::getViewCount);

            case "createtime", "createdat" -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getCreateTime);

            default -> queryWrapper.orderBy(true, isAsc, ScenicSpot::getSortOrder)
                    .orderByDesc(ScenicSpot::getCreateTime);
        }
    }

    /**
     * 批量补齐地区、标签、收藏状态等列表展示字段。
     */
    private List<ScenicListVO> enrichList(List<ScenicSpot> scenicSpots) {
        if (CollectionUtils.isEmpty(scenicSpots)) {
            return Collections.emptyList();
        }
        Map<Long, String> regionNameMap = loadRegionNameMap(scenicSpots);
        Map<Long, List<String>> tagNameMap = loadTagNameMap(scenicSpots);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId, scenicSpots);
        // 批量解析封面图 fileResourceId → URL，避免逐条调用 resolveCoverImageUrl 触发 N+1
        List<Long> coverFileIds = scenicSpots.stream()
                .map(ScenicSpot::getCoverImage)
                .map(FileResourceIds::tryParseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> coverUrlMap = fileService.resolveUrls(coverFileIds);

        return scenicSpots.stream().map(scenicSpot -> {
            ScenicListVO vo = new ScenicListVO();
            vo.setScenicId(scenicSpot.getId());
            vo.setName(scenicSpot.getName());
            vo.setCoverImage(resolveCoverFromMap(scenicSpot.getCoverImage(), coverUrlMap));
            vo.setRegionName(regionNameMap.get(scenicSpot.getRegionId()));
            vo.setScore(scenicSpot.getScore());
            vo.setCategory(scenicSpot.getCategory());
            vo.setLongitude(scenicSpot.getLongitude());
            vo.setLatitude(scenicSpot.getLatitude());
            vo.setLevel(scenicSpot.getLevel());
            vo.setStatus(scenicSpot.getStatus());
            vo.setAddress(scenicSpot.getAddress());
            vo.setOpenTime(scenicSpot.getOpenTime());
            vo.setTicketPrice(scenicSpot.getTicketPrice());
            vo.setTagList(tagNameMap.getOrDefault(scenicSpot.getId(), Collections.emptyList()));
            vo.setIsFavorite(favoriteIds.contains(scenicSpot.getId()));
            return vo;
        }).toList();
    }

    /**
     * 批量加载当前用户对一组景点的收藏关系，避免列表场景下逐条查询导致的 N+1 问题。
     * 未登录或景点列表为空时返回空集合。
     */
    private Set<Long> loadFavoriteIds(Long userId, List<ScenicSpot> scenicSpots) {
        if (userId == null || CollectionUtils.isEmpty(scenicSpots)) {
            return Collections.emptySet();
        }
        List<Long> spotIds = scenicSpots.stream()
                .map(ScenicSpot::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (spotIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .in(UserFavorite::getScenicSpotId, spotIds))
                .stream()
                .map(UserFavorite::getScenicSpotId)
                .collect(Collectors.toSet());
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
        vo.setCoverImage(resolveCoverImageUrl(scenicSpot.getCoverImage()));
        vo.setDescription(scenicSpot.getDescription());
        vo.setDetailContent(scenicSpot.getDetailContent());
        vo.setOpenTime(scenicSpot.getOpenTime());
        vo.setTicketInfo(scenicSpot.getTicketInfo());
        vo.setTicketPrice(scenicSpot.getTicketPrice());
        vo.setLevel(scenicSpot.getLevel());
        vo.setCategory(scenicSpot.getCategory());
        vo.setScore(scenicSpot.getScore());
        vo.setRatingCount(scenicSpot.getRatingCount());
        vo.setViewCount(scenicSpot.getViewCount());
        
        Long realFavCount = scenicSpotMapper.countAllFavorite(scenicSpot.getId());
        vo.setFavoriteCount(realFavCount != null ? realFavCount.intValue() : 0);
        
        Integer reviewCount = queryApprovedReviewCount(scenicSpot.getId());
        vo.setReviewCount(reviewCount);
        
        vo.setHotScore(BigDecimal.valueOf(vo.getFavoriteCount())
            .add(BigDecimal.valueOf(scenicSpot.getViewCount() == null ? 0 : scenicSpot.getViewCount()))
            .add(BigDecimal.valueOf(reviewCount)));
        vo.setBestSeason(scenicSpot.getBestSeason());
        vo.setSuggestedHours(scenicSpot.getSuggestedHours());
        vo.setTips(scenicSpot.getTips());
        vo.setStatus(scenicSpot.getStatus());
        vo.setSortOrder(scenicSpot.getSortOrder());
        vo.setIsRecommended(scenicSpot.getIsRecommended());
        vo.setTagList(loadTagNameMap(List.of(scenicSpot)).getOrDefault(scenicSpot.getId(), Collections.emptyList()));
        vo.setTagIds(loadTagIds(scenicSpot.getId()));
        vo.setImages(listImages(scenicSpot.getId()));
        return vo;
    }

    private Integer queryApprovedReviewCount(Long scenicSpotId) {
        Long count = userReviewMapper.selectCount(new LambdaQueryWrapper<UserReview>()
                .eq(UserReview::getScenicSpotId, scenicSpotId)
            .eq(UserReview::getStatus, REVIEW_STATUS_APPROVED)
                .eq(UserReview::getIsDeleted, 0));
        return count == null ? 0 : Math.toIntExact(count);
    }

    private List<Long> loadTagIds(Long scenicSpotId) {
        return scenicSpotTagMapper.selectByScenicSpotId(scenicSpotId).stream()
                .map(ScenicSpotTag::getTagId)
                .toList();
    }

    /**
     * 批量加载地区名称映射。
     */
    private Map<Long, String> loadRegionNameMap(List<ScenicSpot> scenicSpots) {
        List<Long> regionIds = scenicSpots.stream()
                .map(ScenicSpot::getRegionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(
                        Region::getId,
                        Region::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    /**
     * 批量加载景点标签名称映射。
     */
    private Map<Long, List<String>> loadTagNameMap(List<ScenicSpot> scenicSpots) {
        List<Long> scenicIds = scenicSpots.stream().map(ScenicSpot::getId).distinct().toList();
        if (scenicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ScenicSpotTag> scenicSpotTags = scenicSpotTagMapper.selectByScenicSpotIds(scenicIds);
        if (CollectionUtils.isEmpty(scenicSpotTags)) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = scenicSpotTags.stream().map(ScenicSpotTag::getTagId).distinct().toList();
        Map<Long, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<String>> result = new HashMap<>();
        for (ScenicSpotTag scenicSpotTag : scenicSpotTags) {
            String tagName = tagNameMap.get(scenicSpotTag.getTagId());
            if (tagName == null) {
                continue;
            }
            result.computeIfAbsent(scenicSpotTag.getScenicSpotId(), key -> new ArrayList<>()).add(tagName);
        }
        return result;
    }

    /**
     * 判断当前用户是否已收藏该景点。
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
        if (scenicSpot.getScore() == null) {
            scenicSpot.setScore(0.0);
        }
        if (scenicSpot.getRatingCount() == null) {
            scenicSpot.setRatingCount(0);
        }
        if (scenicSpot.getViewCount() == null) {
            scenicSpot.setViewCount(0L);
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
        // imageIds 为 null 表示前端未传递该字段，跳过同步保持原数据不变
        if (imageIds == null) {
            return;
        }
        // 先删除所有旧图片，再插入新图片
        scenicImageMapper.delete(new LambdaQueryWrapper<ScenicImage>().eq(ScenicImage::getScenicSpotId, scenicSpotId));
        if (imageIds.isEmpty()) {
            return;
        }
        // 校验所有 fileResourceId 有效
        List<FileResource> files = fileResourceMapper.selectBatchIds(imageIds);
        if (files.size() != imageIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部分文件资源不存在");
        }
        // status 字段可能为 null，反转 equals 避免 NPE
        if (files.stream().anyMatch(f -> FileResourceStatus.DELETED.getCode().equals(f.getStatus()))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部分文件资源已删除");
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

        if (!CollectionUtils.isEmpty(scenicImages)) {
            scenicImageMapper.insertBatch(scenicImages);
        }
        fileService.bindFilesToBiz(imageIds, scenicSpotId, BizType.SCENIC);
    }

    private void bindCoverImage(String coverImage, Long scenicSpotId) {
        Long fileId = FileResourceIds.tryParseId(coverImage);
        if (fileId != null) {
            fileService.bindFilesToBiz(List.of(fileId), scenicSpotId, BizType.SCENIC);
        }
        // 旧 URL 格式或空值，不绑定
    }

    /**
     * 详情场景下单个解析。列表路径请走 {@link #enrichList} 里的批量解析。
     */
    private String resolveCoverImageUrl(String coverImage) {
        if (!StringUtils.hasText(coverImage)) {
            return "";
        }
        Long fileId = FileResourceIds.tryParseId(coverImage);
        if (fileId == null) {
            return coverImage.trim();
        }
        return fileService.resolveUrls(List.of(fileId)).getOrDefault(fileId, "");
    }

    /**
     * 列表场景下从预加载好的 Map 取 URL，避免逐条 IO。空字段返回空串；非数字保持旧 URL 原样。
     */
    private String resolveCoverFromMap(String coverImage, Map<Long, String> coverUrlMap) {
        if (!StringUtils.hasText(coverImage)) {
            return "";
        }
        Long fileId = FileResourceIds.tryParseId(coverImage);
        if (fileId == null) {
            return coverImage.trim();
        }
        return coverUrlMap.getOrDefault(fileId, "");
    }

    private ScenicImageVO toImageVO(ScenicImage scenicImage, Map<Long, String> urlMap) {
        ScenicImageVO vo = new ScenicImageVO();
        vo.setId(scenicImage.getId());
        vo.setFileResourceId(scenicImage.getFileResourceId());
        String imageUrl = scenicImage.getImageUrl();
        if (!StringUtils.hasText(imageUrl) && scenicImage.getFileResourceId() != null) {
            imageUrl = urlMap.getOrDefault(scenicImage.getFileResourceId(), "");
        }
        vo.setImageUrl(imageUrl);
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
     * 返回对象中值为 null 的属性名，供 BeanUtils.copyProperties 忽略。
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
