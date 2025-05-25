package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast1h;
import com.zzay.fengxv_weather.service.ITencentWeatherForecast1hService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Zzay
 * @since 2025-05-23
 */
@RestController
@RequestMapping("/tencent-weather-forecast1-h")
public class TencentWeatherForecast1hController {

    @Autowired
    private ITencentWeatherForecast1hService tencentWeatherForecast1hService;

    @Operation(summary = "获取腾讯天气的逐小时天气数据")
    @GetMapping("tencentWeather/{city}")
    public TencentWeatherForecast1HoursDTO getWeatherByTencentWeather(@PathVariable String city) {
        return tencentWeatherForecast1hService.getDataFromTencentWeather(city);
    }


}
