package com.travel.advisor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.ResultCode;
import com.travel.advisor.dto.tag.TagCreateDTO;
import com.travel.advisor.dto.tag.TagUpdateDTO;
import com.travel.advisor.entity.Tag;
import com.travel.advisor.exception.BusinessException;
import com.travel.advisor.mapper.TagMapper;
import com.travel.advisor.service.TagService;
import com.travel.advisor.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<Tag> listByType(Integer type) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getStatus, 1)
                .orderByAsc(Tag::getSortOrder);
        if (type != null) {
            queryWrapper.eq(Tag::getType, type);
        }
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public PageResult<Tag> page(TagCreateDTO dto, PageQuery pageQuery) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>()
                .orderByAsc(Tag::getSortOrder);
        if (dto != null && dto.getType() != null) {
            queryWrapper.eq(Tag::getType, dto.getType());
        }
        if (dto != null && StringUtils.hasText(dto.getName())) {
            queryWrapper.like(Tag::getName, dto.getName());
        }
        if (dto != null && dto.getStatus() != null) {
            queryWrapper.eq(Tag::getStatus, dto.getStatus());
        }

        Page<Tag> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<Tag> result = tagMapper.selectPage(page, queryWrapper);

        return PageResult.<Tag>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .pageNum(Math.toIntExact(result.getCurrent()))
                .pageSize(Math.toIntExact(result.getSize()))
                .totalPage(result.getPages())
                .build();
    }

    @Override
    public Long create(TagCreateDTO dto) {
        Tag tag = BeanCopyUtils.copy(dto, Tag.class);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void update(Long id, TagUpdateDTO dto) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        Tag tag = BeanCopyUtils.copy(dto, Tag.class);
        tag.setId(id);
        tagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        tagMapper.deleteById(id);
    }
}
