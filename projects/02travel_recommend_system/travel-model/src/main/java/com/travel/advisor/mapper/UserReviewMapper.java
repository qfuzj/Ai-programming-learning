package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.UserReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserReviewMapper extends BaseMapper<UserReview> {

  /**
   * 根据景点ID查询平均评分
   */
    @Select("""
            SELECT AVG(rating)
            FROM user_review
            WHERE scenic_spot_id = #{scenicSpotId}
              AND is_deleted = 0
            """)
    Double selectAverageRatingByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    /**
     * 根据景点ID查询评论数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM user_review
            WHERE scenic_spot_id = #{scenicSpotId}
              AND is_deleted = 0
            """)
    Integer countByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);
}
