package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.WeatherAmapSparkDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherAmapSparkHumidityDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherMapDataDTO;
import com.zzay.fengxv_weather.domain.po.CurrrentWeatherAmap;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.ICurrrentWeatherAmapService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 高德天气API数据表 前端控制器
 * </p>
 *
 * @author Zzay
 * @since 2025-10-08
 */
@RestController
@RequestMapping("/currrent-weather-amap")
public class CurrrentWeatherAmapController {
    @Autowired
    private ICurrrentWeatherAmapService currrentWeatherAmapService;
    @Operation(summary = "根据城市名获取天气信息")
    @GetMapping("/{cityName}")
    public Result<CurrrentWeatherAmap> getWeatherByCityName(@PathVariable String cityName) {
        return Result.success(currrentWeatherAmapService.getWeatherByCityName(cityName));
    }


    @Operation(summary = "获取用于地图展示的天气数据")
    @GetMapping("/map-data")
    public Result<List<WeatherMapDataDTO>> getWeatherDataForMap() {
        return Result.success(currrentWeatherAmapService.getWeatherDataForMap());
    }


    @Operation(summary = "Spark分析天气数据")
    @GetMapping("/spark/get_data/temperature")
    public Result<List<WeatherAmapSparkDTO>> getSparkDataForMap() {
        return Result.success(currrentWeatherAmapService.getWeatherSpark());
    }


    @Operation(summary = "Spark分析湿度数据")
    @GetMapping("/spark/get_data/humidity")
    public Result<List<WeatherAmapSparkHumidityDTO>> getSparkHumidityForMap() {
        return Result.success(currrentWeatherAmapService.getWeatherSparkHumidity());
    }

}
