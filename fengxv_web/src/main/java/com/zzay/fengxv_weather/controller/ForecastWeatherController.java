package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.service.IForecastWeatherService;
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
 * @since 2025-05-16
 */
@RestController
@RequestMapping("/forecast-weather")
public class ForecastWeatherController {

    @Autowired
    private IForecastWeatherService forecastWeatherService;

    @Operation(summary = "获取未来天气5天每3h预报")
    @GetMapping("/getForecastWeather/{localCityName}")
    public String getForecastWeather(@PathVariable String localCityName) {

        return forecastWeatherService.get5Day3hourForecastWeather(localCityName);
    }

}
