package com.travel.advisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.advisor.entity.LlmCallLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface LlmCallLogMapper extends BaseMapper<LlmCallLog> {
    Map<String, Object> selectCallLogSummary();
}
