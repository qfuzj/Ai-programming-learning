package com.travel.advisor.service;

import com.travel.advisor.common.page.PageQuery;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.vo.recommend.RecommendItemVO;

/**
 * 推荐服务接口，定义了获取首页推荐和景点相似推荐的方法，返回分页结果，供前端调用以展示个性化的推荐内容
 */
public interface RecommendService {

    /**
     * 获取首页推荐，基于用户的兴趣和行为数据，通过推荐算法生成个性化的推荐列表，返回分页结果以支持前端展示和分页加载
     * @param pageQuery 分页查询参数，包含页码和每页大小等信息，用于控制返回结果的分页展示
     * @return 分页结果，包含推荐项的列表和分页信息，供前端展示个性化的推荐内容
     */
    PageResult<RecommendItemVO> homeRecommend(PageQuery pageQuery);

    /**
     * 获取景点相似推荐，基于指定景点ID，通过相似度计算和推荐算法生成与该景点相似的推荐列表，返回分页结果以支持前端展示和分页加载
     * @param scenicId 景点ID，表示需要获取相似推荐的目标景点，通过该ID进行相似度计算和推荐生成
     * @param pageQuery 分页查询参数，包含页码和每页大小等信息，用于控制返回结果的分页展示
     * @return 分页结果，包含与指定景点相似的推荐项的列表和分页信息，供前端展示个性化的相似推荐内容
     */
    PageResult<RecommendItemVO> scenicSimilarRecommend(Long scenicId, PageQuery pageQuery);
}
