package com.campus.resourcesharing.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserLookupService {

    private final SysUserService sysUserService;

    public AdminUserLookupService(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    public Set<Long> findUserIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptySet();
        }

        String trimmed = keyword.trim();
        return sysUserService.list(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .and(w -> w.like(SysUser::getUsername, trimmed).or().like(SysUser::getNickname, trimmed)))
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());
    }

    public Map<Long, String> buildUserDisplayNameMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return sysUserService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(SysUser::getId, this::displayName, (a, b) -> a));
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        return user.getUsername();
    }
}
