package com.zzay.fengxv_weather.domain.dto;

import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast1h;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TencentWeatherForecast1HoursDTO {
    private Map<String, TencentWeatherForecast1h> forecast1h;
}
