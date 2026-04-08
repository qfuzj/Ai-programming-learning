package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.scenic.ScenicCreateDTO;
import com.travel.advisor.dto.scenic.ScenicImageCreateDTO;
import com.travel.advisor.dto.scenic.ScenicQueryDTO;
import com.travel.advisor.dto.scenic.ScenicStatusDTO;
import com.travel.advisor.dto.scenic.ScenicUpdateDTO;
import com.travel.advisor.entity.ScenicSpot;
import com.travel.advisor.vo.scenic.ScenicDetailVO;
import com.travel.advisor.vo.scenic.ScenicFilterOptionsVO;
import com.travel.advisor.vo.scenic.ScenicImageVO;
import com.travel.advisor.vo.scenic.ScenicListVO;

import java.util.Collection;
import java.util.List;

public interface ScenicSpotService {

    /**
     * 根据 ID 列表和状态查询景点列表
     *
     * @param ids    景点 ID 集合
     * @param status 景点状态（null 时不限制状态）
     * @return 符合条件的景点列表
     */
    List<ScenicSpot> listByIdsWithStatus(Collection<Long> ids, Integer status);

    PageResult<ScenicListVO> page(ScenicQueryDTO dto);

    ScenicDetailVO detail(Long id);

    List<ScenicListVO> hotList();

    ScenicFilterOptionsVO filterOptions();

    Long create(ScenicCreateDTO dto);

    void update(Long id, ScenicUpdateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void batchUpdateStatus(ScenicStatusDTO dto);

    void updateTags(Long scenicSpotId, List<Long> tagIds);

    List<ScenicImageVO> listImages(Long scenicSpotId);

    Long addImage(Long scenicSpotId, ScenicImageCreateDTO dto);

    void deleteImage(Long scenicSpotId, Long imageId);
}