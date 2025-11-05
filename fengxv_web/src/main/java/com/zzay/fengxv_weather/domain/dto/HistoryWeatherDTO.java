package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryWeatherDTO {
    private String city;
    private Integer month;
    private Integer maxTemp;
    private Integer minTemp;
}
