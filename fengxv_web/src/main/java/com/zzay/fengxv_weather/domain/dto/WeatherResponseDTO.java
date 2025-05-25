package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherResponseDTO {
    private String cityName;
    private List<WeatherDetailDTO> forecasts;
}

@Data
class WeatherDetailDTO {
    private String dateTime;
    private Double temperature;
    private Integer humidity;
    private String weatherDescription;
    private String weatherIcon;
}