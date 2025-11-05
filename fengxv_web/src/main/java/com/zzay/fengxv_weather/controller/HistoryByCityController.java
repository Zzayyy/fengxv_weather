package com.zzay.fengxv_weather.controller;


import com.zzay.fengxv_weather.domain.dto.HistoryByCityDTO;
import com.zzay.fengxv_weather.domain.dto.HistoryWeatherDTO;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.IHistoryByCityService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historyByCity")
public class HistoryByCityController {

    @Autowired
    private IHistoryByCityService historyByCityService;
    @Operation(summary = "根据城市获取历史天气")
    @GetMapping("/{city}")
    public Result<List<HistoryByCityDTO>> getHistoryWeatherByCity(@PathVariable String city) {
        return Result.success(historyByCityService.getHistoryWeatherByCity(city));
    }

    @Operation(summary = "最高低温度")
    @GetMapping("/spark/MaxMinTemp/{city}")
    public Result<List<HistoryWeatherDTO>> getMaxMinTempByMonth(@PathVariable String city) {
        return Result.success(historyByCityService.getMaxMinTempByMonth(city));
    }


}
