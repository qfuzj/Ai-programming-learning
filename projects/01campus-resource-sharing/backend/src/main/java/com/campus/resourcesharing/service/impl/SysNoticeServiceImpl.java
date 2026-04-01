package com.campus.resourcesharing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.resourcesharing.entity.SysNotice;
import com.campus.resourcesharing.mapper.SysNoticeMapper;
import com.campus.resourcesharing.service.SysNoticeService;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
}
