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
import com.travel.advisor.vo.user.AdminUserDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserManageServiceImpl implements AdminUserManageService {

    private final UserMapper userMapper;
    private final RegionMapper regionMapper;
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

        return users.stream().map(user -> buildVO(user, regionMap.get(user.getRegionId()))).toList();
    }

    private AdminUserDetailVO buildVO(User user) {
        Region region = user.getRegionId() == null ? null : regionMapper.selectById(user.getRegionId());
        return buildVO(user, region);
    }

    private AdminUserDetailVO buildVO(User user, Region region) {
        AdminUserDetailVO vo = new AdminUserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
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

    private User findUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}