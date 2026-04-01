package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
