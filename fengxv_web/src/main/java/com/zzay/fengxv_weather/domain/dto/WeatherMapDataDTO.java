package com.zzay.fengxv_weather.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherMapDataDTO {
    private String name;  // 城市名
    private Double value; // 温度值
}
