package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.dto.history.BrowseHistoryCreateDTO;
import com.travel.advisor.vo.history.BrowseHistoryVO;

public interface BrowseHistoryService {

    void report(BrowseHistoryCreateDTO dto);

    PageResult<BrowseHistoryVO> page(PageQuery pageQuery);

    void deleteById(Long id);

    void clear();
}
