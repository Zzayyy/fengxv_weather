package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.WeatherLishiAnalysisDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.IWeatherLishi2025Service;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
@RestController
@RequestMapping("/weather-lishi2025")
public class WeatherLishi2025Controller {
    @Autowired
    private IWeatherLishi2025Service weatherLishi2025Service;

    @Operation(summary = "还行吗使用 Spark 分析2025年月度天气数据 - 按城市")
    @GetMapping("/spark/analyze/{city}")
    public Result<WeatherLishiAnalysisDTO> analyzeWeatherWithSparkByCity(@PathVariable String city) {
        return Result.success(weatherLishi2025Service.analyzeWeatherByCity(city));
    }

    @Operation(summary = "使用 Spark 分析2025年月度天气数据 - 全国")
    @GetMapping("/spark/analyze/")
    public Result<WeatherLishiAnalysisDTO> analyzeWeatherWithSparkAllPlace() {
        return Result.success(weatherLishi2025Service.analyzeWeatherAllPlace());
    }

}
