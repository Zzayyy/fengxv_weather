package com.zzay.fengxv_weather.mapper;

import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 每月天气分析结果表 Mapper 接口
 * </p>
 *
 * @author Zzay
 * @since 2025-10-06
 */
public interface WeatherLishi2025MonthlyMapper extends BaseMapper<WeatherLishi2025Monthly> {

    @Select("select * from weather_lishi2025_monthly where city_place = #{cityPlace}")
    List<WeatherLishi2025Monthly> selectByCityPlace(String nationwide);
}
