package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.po.ForecastWeather;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-16
 */
public interface IForecastWeatherService extends IService<ForecastWeather> {

    String get5Day3hourForecastWeather(String localCityName);
}
