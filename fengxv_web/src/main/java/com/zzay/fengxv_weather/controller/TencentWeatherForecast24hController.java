package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast24HoursDTO;
import com.zzay.fengxv_weather.service.ITencentWeatherForecast1hService;
import com.zzay.fengxv_weather.service.ITencentWeatherForecast24hService;
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
 * @since 2025-05-24
 */
@RestController
@RequestMapping("/tencent-weather-forecast24-h")
public class TencentWeatherForecast24hController {

    @Autowired
    private ITencentWeatherForecast24hService  tencentWeatherForecast24hService;

    @Operation(summary = "获取腾讯天气未来7天的24小时数据")
    @GetMapping("tencentWeather/{city}")
    public TencentWeatherForecast24HoursDTO getWeatherByTencentWeather(@PathVariable String city) {
        return tencentWeatherForecast24hService.getDataFromTencentWeather24h(city);
    }

}
