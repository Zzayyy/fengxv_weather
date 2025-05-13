package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentWeatherDTO {
    private int cityId;
    private String weatherMain;
    private String weatherDescription;
    private String icon;
    private BigDecimal temp;
    private BigDecimal feelsLike;
    private BigDecimal tempMin;
    private BigDecimal tempMax;
    private int pressure;
    private int humidity;
    private int seaLevel;
    private int grndLevel;
    private int visibility;
    private BigDecimal windSpeed;
    private int windDeg;
    private BigDecimal windGust;
    private int cloudAll;
    private int dt;
    private String cityName;
}
