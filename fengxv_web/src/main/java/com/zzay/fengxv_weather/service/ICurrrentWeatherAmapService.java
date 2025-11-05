package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.WeatherAmapSparkDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherAmapSparkHumidityDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherMapDataDTO;
import com.zzay.fengxv_weather.domain.po.CurrrentWeatherAmap;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 高德天气API数据表 服务类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-08
 */
public interface ICurrrentWeatherAmapService extends IService<CurrrentWeatherAmap> {

    CurrrentWeatherAmap getWeatherByCityName(String cityName);

    List<WeatherMapDataDTO> getWeatherDataForMap();

    List<WeatherAmapSparkDTO> getWeatherSpark();

    List<WeatherAmapSparkHumidityDTO> getWeatherSparkHumidity();
}
