package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.user.UserQueryDTO;
import com.travel.advisor.entity.Region;
import com.travel.advisor.entity.User;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.RegionMapper;
import com.travel.advisor.mapper.UserMapper;
import com.travel.advisor.security.TokenService;
import com.travel.advisor.service.AdminUserManageService;
import com.travel.advisor.service.FileService;
import com.travel.advisor.utils.FileResourceIds;
import com.travel.advisor.vo.user.AdminUserDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserManageServiceImpl implements AdminUserManageService {

    private final UserMapper userMapper;
    private final RegionMapper regionMapper;
    private final FileService fileService;
    private final TokenService tokenService;

    @Override
    public PageResult<AdminUserDetailVO> page(UserQueryDTO queryDTO) {
        UserQueryDTO query = queryDTO == null ? new UserQueryDTO() : queryDTO;
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(query.getKeyword(), query.getStatus());
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<User> result = userMapper.selectPage(page, wrapper);

        return PageResult.<AdminUserDetailVO>builder()
                .records(buildVOList(result.getRecords()))
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public AdminUserDetailVO detail(Long id) {
        User user = findUserById(id);
        return buildVO(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值不合法");
        }

        User user = findUserById(id);
        if (!Objects.equals(user.getStatus(), status)) {
            user.setStatus(status);
            userMapper.updateById(user);
        }

        if (status == 0) {
            tokenService.invalidateUserSessions(id);
        }
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(inner -> inner
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        return wrapper;
    }

    private List<AdminUserDetailVO> buildVOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> regionIds = users.stream()
                .map(User::getRegionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        var regionMap = regionIds.isEmpty()
                ? Collections.<Long, Region>emptyMap()
                : regionMapper.selectBatchIds(regionIds).stream().collect(Collectors.toMap(Region::getId, Function.identity(), (r1, r2) -> r1));

        Map<Long, String> avatarUrlMap = loadAvatarUrlMap(users);

        return users.stream().map(user -> buildVO(user, regionMap.get(user.getRegionId()), avatarUrlMap)).toList();
    }

    private AdminUserDetailVO buildVO(User user) {
        Region region = user.getRegionId() == null ? null : regionMapper.selectById(user.getRegionId());
        return buildVO(user, region, loadAvatarUrlMap(List.of(user)));
    }

    private AdminUserDetailVO buildVO(User user, Region region, Map<Long, String> avatarUrlMap) {
        AdminUserDetailVO vo = new AdminUserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(resolveAvatarUrl(user.getAvatar(), avatarUrlMap));
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setRegionId(user.getRegionId());
        vo.setRegionName(region == null ? null : region.getName());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreatedAt(user.getCreateTime());
        vo.setUpdatedAt(user.getUpdateTime());
        return vo;
    }

    /**
     * 批量加载用户头像 fileResourceId → URL 的映射，避免逐条查询
     */
    private Map<Long, String> loadAvatarUrlMap(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> fileIds = users.stream()
                .map(User::getAvatar)
                .map(FileResourceIds::tryParseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return fileService.resolveUrls(fileIds);
    }

    /**
     * 将存储的头像值（可能是 fileResourceId 或旧版 URL）解析为可访问 URL。
     */
    private String resolveAvatarUrl(String avatar, Map<Long, String> avatarUrlMap) {
        if (!StringUtils.hasText(avatar)) {
            return "";
        }
        Long fileId = FileResourceIds.tryParseId(avatar);
        if (fileId == null) {
            // 兼容旧数据：直接返回原 URL
            return avatar.trim();
        }
        return avatarUrlMap.getOrDefault(fileId, "");
    }

    private User findUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}