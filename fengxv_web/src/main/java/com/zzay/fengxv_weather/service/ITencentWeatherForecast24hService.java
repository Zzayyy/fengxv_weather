package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast24HoursDTO;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast24h;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-24
 */
public interface ITencentWeatherForecast24hService extends IService<TencentWeatherForecast24h> {

    List<TencentWeatherForecast24h> getDataFromTencentWeather24h(String city);
}
