package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAmapSparkHumidityDTO {
    private String province;
    private Float humidityFloat;
}
