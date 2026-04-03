package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.ScenicImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScenicImageMapper extends BaseMapper<ScenicImage> {

    @Select("SELECT * FROM scenic_image WHERE scenic_spot_id = #{scenicSpotId} AND is_deleted = 0 ORDER BY is_cover DESC, sort_order ASC, id ASC")
    List<ScenicImage> selectByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    void insertBatch(@Param("list") List<ScenicImage> list);
}