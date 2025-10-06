package com.zzay.fengxv_weather.mapper;

import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Analysis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 天气分析结果表 Mapper 接口
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
public interface WeatherLishi2025AnalysisMapper extends BaseMapper<WeatherLishi2025Analysis> {

    @Select("SELECT * FROM weather_lishi2025_analysis WHERE city = #{city}")
    WeatherLishi2025Analysis selectByCity(String cacheKey);
}
