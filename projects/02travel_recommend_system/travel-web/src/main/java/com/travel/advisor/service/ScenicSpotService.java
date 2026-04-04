package com.travel.advisor.service;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.scenic.ScenicCreateDTO;
import com.travel.advisor.dto.scenic.ScenicImageCreateDTO;
import com.travel.advisor.dto.scenic.ScenicQueryDTO;
import com.travel.advisor.dto.scenic.ScenicStatusDTO;
import com.travel.advisor.dto.scenic.ScenicUpdateDTO;
import com.travel.advisor.vo.scenic.ScenicDetailVO;
import com.travel.advisor.vo.scenic.ScenicFilterOptionsVO;
import com.travel.advisor.vo.scenic.ScenicImageVO;
import com.travel.advisor.vo.scenic.ScenicListVO;

import java.util.List;

public interface ScenicSpotService {

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