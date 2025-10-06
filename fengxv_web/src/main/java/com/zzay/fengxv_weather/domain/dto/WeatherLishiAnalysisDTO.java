// WeatherAnalysisDTO.java
package com.zzay.fengxv_weather.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherLishiAnalysisDTO implements Serializable {
    private String city;
    private Long sunnyDays;
    private Long rainyDays;
    private Long cloudyDays;
    private Long snowyDays;
    private Double avgMinTemp;
    private Double avgMaxTemp;
    private Integer hottestDayMaxTemp;
    private Integer coldestDayMinTemp;
    private String mostCommonWindDay;
    private String mostCommonWindNight;
    private Long totalRecords;
}