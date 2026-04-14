package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {

    @Select("SELECT COUNT(1) FROM user_favorite WHERE user_id = #{userId} AND scenic_spot_id = #{scenicSpotId} AND is_deleted = 0")
    Long countUserFavorite(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);

    @Select("SELECT COUNT(1) FROM user_favorite WHERE scenic_spot_id = #{scenicSpotId} AND is_deleted = 0")
    Long countAllFavorite(@Param("scenicSpotId") Long scenicSpotId);

    @Select("SELECT DISTINCT category FROM scenic_spot WHERE is_deleted = 0 AND status = 1 AND category IS NOT NULL AND category <> '' ORDER BY category")
    List<String> selectCategories();

    @Select("SELECT DISTINCT level FROM scenic_spot WHERE is_deleted = 0 AND status = 1 AND level IS NOT NULL AND level <> '' ORDER BY level")
    List<String> selectLevels();
}