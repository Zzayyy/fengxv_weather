package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast1h;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-23
 */
public interface ITencentWeatherForecast1hService extends IService<TencentWeatherForecast1h> {
    List<TencentWeatherForecast1h> getDataFromTencentWeather(String city);

}
