package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.region.RegionCreateDTO;
import com.travel.advisor.dto.region.RegionUpdateDTO;
import com.travel.advisor.entity.Region;
import com.travel.advisor.vo.region.RegionTreeVO;

import java.util.List;

public interface RegionService {

    /**
     * 获取地区树形结构
     */
    List<RegionTreeVO> getTree();

    /**
     * 管理端分页查询地区
     */
    PageResult<Region> page(RegionCreateDTO dto, PageQuery pageQuery);

    /**
     * 新增地区
     */
    Long create(RegionCreateDTO dto);

    /**
     * 更新地区
     */
    void update(Long id, RegionUpdateDTO dto);

    /**
     * 删除地区
     */
    void delete(Long id);
}
