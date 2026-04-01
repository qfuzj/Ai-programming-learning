package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.entity.SysUser;
import com.campus.resourcesharing.mapper.SysUserMapper;
import com.campus.resourcesharing.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
