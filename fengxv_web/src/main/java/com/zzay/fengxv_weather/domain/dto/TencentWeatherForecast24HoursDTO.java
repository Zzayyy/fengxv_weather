package com.zzay.fengxv_weather.domain.dto;

import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast24h;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TencentWeatherForecast24HoursDTO {
    private Map<String, TencentWeatherForecast24h> forecast24h;
}
