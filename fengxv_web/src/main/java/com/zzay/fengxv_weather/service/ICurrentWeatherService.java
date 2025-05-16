package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.CurrentWeatherDTO;
import com.zzay.fengxv_weather.domain.po.CurrentWeather;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-08
 */
public interface ICurrentWeatherService extends IService<CurrentWeather> {

    CurrentWeatherDTO getCurrentWeatherByCityName(String city);

    String getCurrentWeatherBYCityId(String cityId);

    String getCurrentWeatherByZIPCode(String zipCode);
}
