package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.WeatherLishiAnalysisDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
public interface IWeatherLishi2025Service extends IService<WeatherLishi2025> {

    WeatherLishiAnalysisDTO analyzeWeatherByCity(String city);

    WeatherLishiAnalysisDTO analyzeWeatherAllPlace();

}
