package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    
    @Select("SELECT * FROM user_favorite WHERE user_id = #{userId} AND scenic_spot_id = #{scenicSpotId}")
    UserFavorite selectWithDeleted(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);

    @Update("UPDATE user_favorite SET is_deleted = 0, update_time = NOW() WHERE id = #{id}")
    int restoreDeleted(@Param("id") Long id);
}
