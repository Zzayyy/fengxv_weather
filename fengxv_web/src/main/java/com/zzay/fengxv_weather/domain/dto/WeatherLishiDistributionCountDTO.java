package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherLishiDistributionCountDTO {
    private String highTempWeatherCount;
    private String lowTempWeatherCount;
    private String comfortableTempWeatherCount;

    private String highestWeather;
    private String lowestWeather;

}
