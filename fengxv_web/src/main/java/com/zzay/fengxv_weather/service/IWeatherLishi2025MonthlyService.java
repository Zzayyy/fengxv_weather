package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.WeatherLishi2025MonthlyDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherLishiDistributionCountDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 每月天气分析结果表 服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-06
 */
public interface IWeatherLishi2025MonthlyService extends IService<WeatherLishi2025Monthly> {

    List<WeatherLishi2025Monthly> getMonthlyTemperatureNationwide();

    WeatherLishiDistributionCountDTO getTempDistributionCount();
}
