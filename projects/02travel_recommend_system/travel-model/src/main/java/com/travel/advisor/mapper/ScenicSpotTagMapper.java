package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.ScenicSpotTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScenicSpotTagMapper extends BaseMapper<ScenicSpotTag> {

    @Select("SELECT * FROM scenic_spot_tag WHERE scenic_spot_id = #{scenicSpotId} ORDER BY id ASC")
    List<ScenicSpotTag> selectByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    @Select({
            "<script>",
            "SELECT * FROM scenic_spot_tag",
            "WHERE scenic_spot_id IN",
            "<foreach collection='scenicSpotIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "ORDER BY scenic_spot_id ASC, id ASC",
            "</script>"
    })
    List<ScenicSpotTag> selectByScenicSpotIds(@Param("scenicSpotIds") List<Long> scenicSpotIds);

    @Delete("DELETE FROM scenic_spot_tag WHERE scenic_spot_id = #{scenicSpotId}")
    int deleteByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    List<String> selectTagNamesByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    Integer insertBatch(@Param("list") List<ScenicSpotTag> list);
}