package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.WeatherLishi2025MonthlyDTO;
import com.zzay.fengxv_weather.domain.dto.WeatherLishiDistributionCountDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.IWeatherLishi2025MonthlyService;
import com.zzay.fengxv_weather.service.IWeatherLishi2025Service;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 每月天气分析结果表 前端控制器
 * </p>
 *
 * @author Zzay
 * @since 2025-10-06
 */
@RestController
@RequestMapping("/weather-lishi2025-monthly")
public class WeatherLishi2025MonthlyController {

    @Autowired
    private IWeatherLishi2025MonthlyService weatherLishi2025MonthlyService;

    @Operation(summary = "获取全国月度温度统计")
    @GetMapping("/temperature/nationwide")
    public Result<List<WeatherLishi2025Monthly>> getMonthlyTemperatureNationwide() {
        return Result.success(weatherLishi2025MonthlyService.getMonthlyTemperatureNationwide());
    }

    @Operation(summary = "获取全国高温天，低温天，适中天数量统计")
    @GetMapping("/temperature/distribution")
    public Result<WeatherLishiDistributionCountDTO> getTempDistributionCount() {
        return Result.success(weatherLishi2025MonthlyService.getTempDistributionCount());
    }
}
