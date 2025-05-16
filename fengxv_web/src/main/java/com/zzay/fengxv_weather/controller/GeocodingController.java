package com.zzay.fengxv_weather.controller;

import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.service.GeocodingService;
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
 * @since 2025-05-08
 */
@RestController
@RequestMapping("/geocoding")
public class GeocodingController {

    @Autowired
    private GeocodingService geocodingService;

    @Operation(summary = "根据城市名获取经纬度")
    @GetMapping("/{localCityName}")
    public Result<GeocodingDTO> getGeocodingByCityName(@PathVariable String localCityName) {
        return Result.success(geocodingService.getGeocodingByCityName(localCityName));
    }
}
