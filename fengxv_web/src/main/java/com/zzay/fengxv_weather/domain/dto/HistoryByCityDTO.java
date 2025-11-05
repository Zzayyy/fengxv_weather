package com.zzay.fengxv_weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryByCityDTO {
    @JsonProperty("date")
    private String date;

    @JsonProperty("weatherDay")
    private String weatherDay;

    @JsonProperty("weatherNight")
    private String weatherNight;

    @JsonProperty("minTemp")
    private String minTemp;

    @JsonProperty("maxTemp")
    private String maxTemp;

    @JsonProperty("windDay")
    private String windDay;

    @JsonProperty("windNight")
    private String windNight;
}

