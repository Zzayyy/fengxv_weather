package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.CurrentWeatherDTO;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.ICurrentWeatherService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Zzay
 * @since 2025-05-08
 */
@RestController
@RequestMapping("/current-weather")
public class CurrentWeatherController {

    @Autowired
    private ICurrentWeatherService currentWeatherService;

    @Operation(summary = "根据城市名获取天气信息")
    @GetMapping("/{city}")
    public Result<CurrentWeatherDTO> getWeatherByCity(@PathVariable String city) {
        return Result.success(currentWeatherService.getCurrentWeatherByCityName(city));
    }


    @Operation(summary = "根据城市id获取天气信息")
    @GetMapping("/cityId/{cityId}")
    public String getWeatherByCityId(@PathVariable String cityId) {
        return currentWeatherService.getCurrentWeatherBYCityId(cityId);
    }


    @Operation(summary = "根据邮政Code获取天气信息")
    @GetMapping("/ZIPCode/{ZIPCode}")
    public String getWeatherByZIPCode(@PathVariable String ZIPCode) {
        return currentWeatherService.getCurrentWeatherByZIPCode(ZIPCode);
    }

}
