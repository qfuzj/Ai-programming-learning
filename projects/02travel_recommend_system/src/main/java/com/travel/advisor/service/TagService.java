package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.tag.TagCreateDTO;
import com.travel.advisor.dto.tag.TagUpdateDTO;
import com.travel.advisor.entity.Tag;

import java.util.List;

public interface TagService {

    /**
     * 查询所有启用的标签
     */
    List<Tag> listByType(Integer type);

    /**
     * 管理端分页查询标签
     */
    PageResult<Tag> page(TagCreateDTO dto, PageQuery pageQuery);

    /**
     * 新增标签
     */
    Long create(TagCreateDTO dto);

    /**
     * 更新标签
     */
    void update(Long id, TagUpdateDTO dto);

    /**
     * 删除标签（逻辑删除）
     */
    void delete(Long id);
}
